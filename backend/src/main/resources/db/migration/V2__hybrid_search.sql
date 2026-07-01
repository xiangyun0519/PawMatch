-- ============================================
-- V2__hybrid_search.sql
-- 阶段三：混合检索升级
--   - knowledge_base 增加 tsv 列（中文全文检索）
--   - GIN 全文索引
-- ============================================

-- 增加 tsvector 列：title + content + chunk_content 全文检索
ALTER TABLE knowledge_base
    ADD COLUMN IF NOT EXISTS tsv tsvector;

-- 触发器：插入/更新时自动生成 tsv
CREATE OR REPLACE FUNCTION knowledge_base_tsv_update() RETURNS trigger AS $$
BEGIN
    NEW.tsv :=
        setweight(to_tsvector('simple', coalesce(NEW.title, '')), 'A') ||
        setweight(to_tsvector('simple', coalesce(NEW.content, '')), 'B') ||
        setweight(to_tsvector('simple', coalesce(NEW.chunk_content, '')), 'C');
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_knowledge_base_tsv ON knowledge_base;
CREATE TRIGGER trg_knowledge_base_tsv
    BEFORE INSERT OR UPDATE OF title, content, chunk_content
    ON knowledge_base
    FOR EACH ROW EXECUTE FUNCTION knowledge_base_tsv_update();

-- GIN 全文索引
CREATE INDEX IF NOT EXISTS idx_knowledge_tsv ON knowledge_base USING gin (tsv);