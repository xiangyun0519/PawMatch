-- ============================================
-- 流浪动物领养平台 - 测试数据
-- 密码统一使用: $2a$10$JmmztACzJVfeomRiaBswPuEUxNTtBX3Qtpn8DpcoIlFYiKtIWNb8u
-- ============================================

-- 用户表测试数据
INSERT INTO "user" (username, password_hash, phone, email, role, avatar_url, status) VALUES
('zhangsan', '$2a$10$JmmztACzJVfeomRiaBswPuEUxNTtBX3Qtpn8DpcoIlFYiKtIWNb8u', '13800138001', 'zhangsan@example.com', 'ADOPTER', 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhangsan', 1),
('lisi', '$2a$10$JmmztACzJVfeomRiaBswPuEUxNTtBX3Qtpn8DpcoIlFYiKtIWNb8u', '13800138002', 'lisi@example.com', 'ADOPTER', 'https://api.dicebear.com/7.x/avataaars/svg?seed=lisi', 1),
('wangwu', '$2a$10$JmmztACzJVfeomRiaBswPuEUxNTtBX3Qtpn8DpcoIlFYiKtIWNb8u', '13800138003', 'wangwu@example.com', 'ADOPTER', 'https://api.dicebear.com/7.x/avataaars/svg?seed=wangwu', 1),
('shelter_admin1', '$2a$10$JmmztACzJVfeomRiaBswPuEUxNTtBX3Qtpn8DpcoIlFYiKtIWNb8u', '13800138004', 'shelter1@example.com', 'SHELTER', 'https://api.dicebear.com/7.x/avataaars/svg?seed=shelter1', 1),
('shelter_admin2', '$2a$10$JmmztACzJVfeomRiaBswPuEUxNTtBX3Qtpn8DpcoIlFYiKtIWNb8u', '13800138005', 'shelter2@example.com', 'SHELTER', 'https://api.dicebear.com/7.x/avataaars/svg?seed=shelter2', 1),
('admin', '$2a$10$JmmztACzJVfeomRiaBswPuEUxNTtBX3Qtpn8DpcoIlFYiKtIWNb8u', '13900000000', 'admin@pawmatch.com', 'ADMIN', 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin', 1);

-- 救助站表测试数据
INSERT INTO shelter (user_id, name, type, address, contact_phone, license_number, is_verified, description) VALUES
(4, '阳光宠物救助站', 'GOVERNMENT', '北京市朝阳区望京街道123号', '010-12345678', 'BJ-XKZ-20240001', true, '专业流浪动物救助机构，提供宠物领养、绝育、免疫等服务'),
(5, '爱心动物之家', 'PRIVATE', '上海市浦东新区张江镇456号', '021-87654321', 'SH-XKZ-20240002', true, '民间爱心组织，专注于流浪猫狗的救助与领养');

-- 宠物档案表测试数据
INSERT INTO pet_profile (name, species, breed, age_months, gender, size, health_status, personality_tags, description, photos, shelter_id, status) VALUES
-- 狗狗
('旺财', 'DOG', '中华田园犬', 24, 'MALE', 'MEDIUM', '已绝育、已免疫、身体健康', '["温顺", "亲人", "活泼", "聪明"]', '旺财是一只被遗弃的田园犬，经过救助站悉心照料，现在性格非常温顺亲人，喜欢和人互动。适合有庭院的家庭饲养。', '["https://images.unsplash.com/photo-1548199973-03cce0bbc87b?w=400", "https://images.unsplash.com/photo-1552053831-71594a27632d?w=400"]', 1, 'AVAILABLE'),
('豆豆', 'DOG', '泰迪', 18, 'FEMALE', 'SMALL', '已绝育、已免疫、身体健康', '["粘人", "活泼", "好动", "爱撒娇"]', '豆豆是一只可爱的泰迪犬，性格活泼粘人，特别喜欢和小朋友玩。不掉毛，适合公寓饲养。', '["https://images.unsplash.com/photo-1587300003388-59208cc962cb?w=400"]', 1, 'AVAILABLE'),
('大黄', 'DOG', '金毛', 36, 'MALE', 'LARGE', '已绝育、已免疫、身体健康', '["友好", "忠诚", "服从", "温和"]', '大黄是一只温顺的金毛寻回犬，对人非常友好热情，是孩子的最佳玩伴。有丰富养狗经验的家庭优先。', '["https://images.unsplash.com/photo-1633722715463-d30f4f325e24?w=400"]', 2, 'AVAILABLE'),
('小黑', 'DOG', '拉布拉多', 12, 'MALE', 'LARGE', '已免疫、身体健康', '["热情", "好奇", "活泼", "亲人"]', '小黑是一只年轻的拉布拉多，性格热情活泼，正在寻找一个爱它的家庭。需要较大活动空间。', '["https://images.unsplash.com/photo-1529429617124-95b109e86bb8?w=400"]', 2, 'AVAILABLE'),
-- 猫咪
('咪咪', 'CAT', '中华田园猫', 6, 'FEMALE', 'SMALL', '已绝育、已免疫、身体健康', '["独立", "安静", "爱干净", "害羞"]', '咪咪是一只6个月大的小母猫，性格安静独立，喜欢在窗台上晒太阳看鸟。适合上班族饲养。', '["https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=400"]', 1, 'AVAILABLE'),
('橘座', 'CAT', '橘猫', 48, 'MALE', 'MEDIUM', '已绝育、已免疫、身体健康', '["慵懒", "亲人", "贪吃", "温和"]', '橘座是一只中年橘猫，性格极其温和亲人，胃口很好。喜欢被人抚摸，适合有时间陪伴的家庭。', '["https://images.unsplash.com/photo-1573865526739-10659fec78a5?w=400"]', 1, 'AVAILABLE'),
('小灰', 'CAT', '英国短毛猫', 14, 'MALE', 'MEDIUM', '已绝育、已免疫、身体健康', '["沉稳", "安静", "友好", "易饲养"]', '小灰是一只英短弟弟，性格沉稳安静，不挑食好饲养。非常适合第一次养猫的家庭。', '["https://images.unsplash.com/photo-1533738363-b7f9aef128ce?w=400"]', 2, 'AVAILABLE'),
('小白', 'CAT', '波斯猫', 20, 'FEMALE', 'MEDIUM', '已绝育、已免疫、需定期护理', '["优雅", "安静", "亲人", "粘人"]', '小白是一只漂亮的白波斯猫，毛发需要定期打理。性格优雅安静，非常粘人，需要有时间照顾它的家庭。', '["https://images.unsplash.com/photo-1513245543132-31f507417b26?w=400"]', 2, 'ADOPTED'),
-- 其他宠物
('球球', 'RABBIT', '荷兰垂耳兔', 8, 'FEMALE', 'SMALL', '已免疫、身体健康', '["温顺", "安静", "爱干净", "跳跃"]', '球球是一只可爱的荷兰垂耳兔，性格温顺爱干净。养在笼子里即可，适合居住空间有限的家庭。', '["https://images.unsplash.com/photo-1585110396000-c9ffd4e4b308?w=400"]', 1, 'AVAILABLE'),
('小橘', 'CAT', '橘猫', 3, 'MALE', 'SMALL', '已免疫、身体健康', '["活泼", "好奇", "亲人", "粘人"]', '小橘是一只3个月大的小橘猫，性格活泼好动，对一切充满好奇。正在等待一个爱它的家庭。', '["https://images.unsplash.com/photo-1495360010541-f48722b34f7d?w=400"]', 1, 'AVAILABLE');

-- 领养人画像表测试数据
INSERT INTO adopter_profile (user_id, housing_type, has_children, has_elderly, has_other_pets, pet_experience, daily_hours_available, preferred_pet_size, preferred_pet_age, allergy_info, activity_level, adoption_motivation) VALUES
(1, 'APARTMENT', true, false, false, 'INTERMEDIATE', 6, '["SMALL", "MEDIUM"]', '["0-12", "12-24"]', NULL, 'HIGH', '家里小朋友一直想要养一只宠物，希望给他一个一起成长的伙伴，培养责任感'),
(2, 'HOUSE_WITH_YARD', false, false, true, 'ADVANCED', 8, '["MEDIUM", "LARGE"]', '["12-36"]', NULL, 'VERY_HIGH', '家里已经有一只猫咪，想再领养一只狗狗，让它们互相陪伴'),
(3, 'APARTMENT', false, false, false, 'NONE', 4, '["SMALL"]', '["0-12"]', '轻微猫毛过敏', 'MODERATE', '第一次养宠物，想从一只小型猫开始，慢慢学习如何照顾小动物');

-- 宠物性格标签表测试数据
INSERT INTO pet_personality_tag (pet_id, tag, confidence) VALUES
(1, '温顺', 0.95),
(1, '亲人', 0.92),
(1, '活泼', 0.88),
(1, '聪明', 0.85),
(2, '粘人', 0.94),
(2, '活泼', 0.91),
(2, '好动', 0.89),
(2, '爱撒娇', 0.87),
(3, '友好', 0.96),
(3, '忠诚', 0.93),
(3, '服从', 0.90),
(4, '热情', 0.94),
(4, '好奇', 0.91),
(5, '独立', 0.93),
(5, '安静', 0.89),
(6, '慵懒', 0.95),
(6, '亲人', 0.92),
(7, '沉稳', 0.91),
(7, '安静', 0.88),
(8, '优雅', 0.94),
(8, '安静', 0.90),
(9, '温顺', 0.93),
(10, '活泼', 0.95),
(10, '好奇', 0.92);

-- ============================================
-- 注意：向量数据（pet_embedding、adopter_embedding）在 embeddings.sql 中
-- 请先执行: psql -h localhost -U postgres -d pawmatch -f embeddings.sql
-- ============================================

-- 宠物知识库表测试数据
INSERT INTO knowledge_base (category, title, content, chunk_content, metadata, status) VALUES
('BREED_CARE', '中华田园犬饲养指南', '中华田园犬是中国本土犬种，具有以下特点：\n1. 适应能力强：能适应各种气候和环境\n2. 身体素质好：抵抗力强，较少生病\n3. 性格稳定：忠诚护主，对家人温顺\n4. 易于饲养：不挑食，喂养成本低\n\n饲养注意事项：\n- 定期接种疫苗和驱虫\n- 保持适量运动，每天至少散步两次\n- 注意毛发护理，定期洗澡梳理\n- 提供干净的饮水和均衡的饮食', '中华田园犬是中国本土犬种，适应能力强、身体素质好、性格稳定、易于饲养。需定期接种疫苗、保持运动、注意毛发护理。', '{"category": "BREED_CARE", "species": "DOG", "tags": ["田园犬", "饲养", "指南"]}', 'ACTIVE'),
('HEALTH', '猫咪疫苗接种全攻略', '猫咪需要接种的核心疫苗包括：\n\n1. 猫三联疫苗（FVRCP）\n   - 预防猫瘟、猫鼻支、猫杯状病毒\n   - 首次免疫：8周龄开始，间隔3-4周接种一针，共3针\n   - 每年加强免疫一次\n\n2. 狂犬疫苗\n   - 12周龄以上可接种\n   - 之后每1-3年加强一次\n\n注意事项：\n- 接种前确保猫咪身体健康，无发热、咳嗽等症状\n- 接种后观察30分钟，防止过敏反应\n- 部分猫咪接种后可能出现嗜睡、食欲减退，属正常现象', '{"category": "HEALTH", "species": "CAT", "tags": ["疫苗", "接种", "健康"]}', 'ACTIVE'),
('TRAINING', '幼犬基础训练方法', '幼犬训练是建立良好行为习惯的关键时期，主要包括：\n\n1. 如厕训练\n   - 建立固定的排便地点\n   - 定时带幼犬外出\n   - 及时奖励正确行为\n\n2. 社交化训练\n   - 让幼犬接触不同的人和动物\n   - 逐渐适应各种环境\n   - 培养稳定性格\n\n3. 基础指令\n   - 坐下、趴下、等待、过来等\n   - 使用正向强化方法\n   - 保持训练时间短暂有趣', '{"category": "TRAINING", "species": "DOG", "tags": ["幼犬", "训练", "基础"]}', 'ACTIVE'),
('ADOPTION', '领养流程与注意事项', '领养宠物是一个严肃的决定，请确保：\n\n领养前准备：\n1. 获得所有家庭成员的同意\n2. 评估自己的经济能力（食物、医疗、用品）\n3. 确保有足够的时间和精力陪伴\n4. 了解当地养犬/养猫规定\n\n领养流程：\n1. 选择合适的救助机构\n2. 填写领养申请表\n3. 接受家访/面试\n4. 签署领养协议\n5. 支付领养费用（部分机构）\n6. 带宠物回家\n\n领养后注意事项：\n- 预留一周左右的适应期\n- 定期体检和疫苗接种\n- 及时办理登记手续', '{"category": "ADOPTION", "tags": ["领养", "流程", "注意事项"]}', 'ACTIVE'),
('NUTRITION', '宠物科学喂养指南', '不同年龄段的宠物有不同的营养需求：\n\n幼年期（0-12月）：\n- 需要高蛋白、高脂肪的幼粮\n- 少食多餐，每天3-4顿\n- 注意补钙促进骨骼发育\n\n成年期（1-7岁）：\n- 选择适合品种和体型的成粮\n- 每天喂食2顿\n- 控制食量避免肥胖\n\n老年期（7岁以上）：\n- 选择低脂肪、易消化的老粮\n- 可适当添加关节保健食品\n- 定期体检监测健康\n\n通用注意事项：\n- 随时提供新鲜饮水\n- 避免喂食人类食物中的禁忌（如巧克力、洋葱等）\n- 选择品质可靠的宠物食品', '{"category": "NUTRITION", "tags": ["喂养", "营养", "科学"]}', 'ACTIVE');

-- 领养申请表测试数据
INSERT INTO adoption_application (adopter_id, pet_id, status, matching_score, matching_reasons, applicant_message, shelter_review_note, reviewed_at, completed_at) VALUES
(1, 2, 'APPROVED', 92.50, '用户偏好小型宠物，豆豆体型小巧适合；用户每天6小时陪伴时间充足；豆豆活泼好动的性格与用户高活动水平匹配', '我非常喜欢豆豆，希望能给它一个温暖的家。我有养宠经验，会好好照顾它。', '非常合适的领养人，有中级养宠经验，居住环境适合小型犬。', '2026-03-20 10:30:00', '2026-03-22 14:00:00'),
(1, 5, 'PENDING', 88.30, '用户偏好小型宠物，咪咪符合；用户每天6小时陪伴时间可满足猫咪需求；咪咪独立安静的性格适合上班族', '咪咪太可爱了，希望能领养它。虽然有点害羞，但我相信用爱可以慢慢打开它的心扉。', NULL, NULL, NULL),
(2, 1, 'APPROVED', 95.80, '用户有院子适合中型犬活动；用户高级养宠经验能照顾好田园犬；每天8小时陪伴时间充足；无小孩但有其他宠物可作伴', '我家里已经有一只猫了，想再领养一只狗狗。旺财看起来温顺稳重，正是我想要的。', '优秀的领养人，有丰富养宠经验，居住环境优越，非常放心。', '2026-03-18 15:00:00', '2026-03-19 09:00:00'),
(2, 4, 'PENDING', 90.20, '用户偏好大户型宠物，小黑符合；用户每天8小时陪伴且活动水平很高，适合活泼的拉布拉多；居住环境有院子可提供充足活动空间', '小黑太可爱了！拉布拉多一直是我的最爱，希望能给它最好的生活。', NULL, NULL, NULL),
(3, 5, 'REJECTED', 75.60, '用户有轻微猫毛过敏，可能对猫咪产生不适；咪咪虽然合适但需考虑领养人健康状况；建议选择无毛猫或低过敏性宠物', '我第一次养宠物，对养猫充满期待。请问有什么需要注意的吗？', '领养人有过敏情况，不建议领养猫咪。建议选择低过敏性宠物或先进行过敏测试。', '2026-03-15 11:00:00', NULL),
(3, 9, 'PENDING', 82.40, '用户偏好小型宠物，球球体型小巧；兔子的过敏风险较低，适合有轻微过敏的用户；每天4小时陪伴时间基本满足兔子需求', '球球太可爱了！兔子比较安静，适合我这样的上班族。', NULL, NULL, NULL),
(1, 10, 'APPROVED', 89.50, '用户有小孩，幼猫是很好的成长伙伴；小橘活泼好奇的性格适合有孩子的家庭；用户每天6小时陪伴时间适合幼猫', '小橘好可爱啊！家里小朋友一定会很开心有这个小伙伴一起成长。', '有小朋友的家庭适合养幼猫，注意做好防护措施。批准领养。', '2026-03-24 16:00:00', '2026-03-25 10:00:00');

-- 回访记录表测试数据
INSERT INTO follow_up_record (application_id, days_after_adoption, photos, pet_health_status, pet_behavior_status, adopter_feedback, adoption_satisfaction, issues_found, next_follow_up_date) VALUES
(1, 7, '["https://images.unsplash.com/photo-1587300003388-59208cc962cb?w=400"]', '豆豆适应良好，食欲正常，排便正常', '豆豆已经适应新家，与家人相处融洽，喜欢在客厅跑来跑去', '非常好！豆豆太可爱了，小朋友特别喜欢它。每天都玩得很开心。', 5, NULL, '2026-04-01'),
(1, 30, '["https://images.unsplash.com/photo-1587300003388-59208cc962cb?w=400", "https://images.unsplash.com/photo-1548199973-03cce0bbc87b?w=400"]', '豆豆身体健康，体重略有增加，毛发光泽好', '豆豆已经完全融入家庭，学会了定点排便，每天准时叫我们起床', '太满意了！豆豆是我们的家庭成员了，感谢救助站的悉心照顾。', 5, NULL, '2026-04-22'),
(3, 7, '["https://images.unsplash.com/photo-1548199973-03cce0bbc87b?w=400"]', '旺财适应良好，已完成新环境适应', '旺财与家中猫咪相处需要时间，开始互相试探中', '旺财很乖，就是有点想原来的救助站。慢慢就好了。', 4, NULL, '2026-03-28'),
(3, 30, '["https://images.unsplash.com/photo-1548199973-03cce0bbc87b?w=400"]', '旺财身体健康，与猫咪成为朋友', '旺财和猫咪现在一起吃饭一起睡觉，成为真正的一家人', '太高兴了！当初的选择是正确的，旺财给我们带来很多快乐。', 5, NULL, NULL),
(7, 3, '["https://images.unsplash.com/photo-1495360010541-f48722b34f7d?w=400"]', '小橘状态良好，精神活泼', '小橘对环境好奇，到处探索中，晚上有点闹需要适应', '小橘太活泼了！小朋友开心得不得了，就是晚上有点闹。', 4, '晚上会比较活跃，建议提供猫爬架和玩具分散注意力', '2026-04-01');

-- 聊天会话表测试数据
INSERT INTO chat_session (user_id, title, created_at, updated_at) VALUES
(1, '咨询泰迪犬豆豆的领养', '2026-03-10 14:30:00', '2026-03-10 15:45:00'),
(1, '了解领养流程', '2026-03-12 09:00:00', '2026-03-12 09:30:00'),
(2, '大黄领养咨询', '2026-03-15 10:00:00', '2026-03-15 11:20:00'),
(3, '新手养猫建议', '2026-03-14 16:00:00', '2026-03-14 17:00:00');

-- 聊天消息表测试数据
INSERT INTO chat_message (session_id, role, content, metadata, created_at) VALUES
-- 会话1：咨询泰迪犬豆豆
(1, 'USER', '你好，我想咨询一下泰迪犬豆豆的领养条件', '{"source": "pet_profile", "pet_id": 2}', '2026-03-10 14:30:00'),
(1, 'ASSISTANT', '您好！很高兴您对豆豆感兴趣。豆豆是一只1.5岁的雌性泰迪犬，身体健康，已绝育已免疫。性格活泼粘人，适合公寓饲养。请问您家是什么类型的住房呢？', NULL, '2026-03-10 14:32:00'),
(1, 'USER', '我住在公寓里，有小孩', '{"source": "adopter_profile", "user_id": 1}', '2026-03-10 14:35:00'),
(1, 'ASSISTANT', '公寓养泰迪很适合哦！豆豆体型小，不需要太大的活动空间。它性格活泼，和小朋友相处融洽。不过泰迪比较粘人，需要您有足够时间陪伴它。请问小朋友多大了呢？', NULL, '2026-03-10 14:38:00'),
(1, 'USER', '8岁了，很喜欢宠物', NULL, '2026-03-10 14:40:00'),
(1, 'ASSISTANT', '太棒了！8岁的孩子正是和宠物建立感情的最好年龄。豆豆和这样的家庭非常匹配。我可以帮您提交领养申请吗？需要您提供更多信息来完成匹配评估。', NULL, '2026-03-10 14:42:00'),
(1, 'USER', '好的，请帮我提交申请', NULL, '2026-03-10 15:45:00'),
(1, 'ASSISTANT', '好的！您的领养申请已提交。我们会对您的家庭进行简单评估，请保持电话畅通，救助站会在2-3天内联系您。期待豆豆找到温暖的新家！', NULL, '2026-03-10 15:45:00'),
-- 会话2：了解领养流程
(2, 'USER', '请问领养流程是什么样的？', NULL, '2026-03-12 09:00:00'),
(2, 'ASSISTANT', '您好！我们的领养流程如下：\n1. 选择心仪的宠物并提交申请\n2. 救助站审核您的申请（住房、经济、时间等）\n3. 初步审核通过后，救助站会联系您进行家访\n4. 家访通过后，签署领养协议并支付领养费用\n5. 带宠物回家，开启幸福生活\n6. 领养后需接受定期回访\n\n请问您是想领养什么类型的宠物呢？', NULL, '2026-03-12 09:05:00'),
(2, 'USER', '我想领养一只猫', NULL, '2026-03-12 09:10:00'),
(2, 'ASSISTANT', '好的！我们有多只可爱的猫咪等待领养。请问您有什么偏好吗？比如年龄、性别、品种等？我可以为您推荐最合适的猫咪。', NULL, '2026-03-12 09:12:00'),
(2, 'USER', '想要一只小型猫，好养的那种', NULL, '2026-03-12 09:15:00'),
(2, 'ASSISTANT', '根据您的需求，我推荐您考虑：\n1. 咪咪 - 6个月大的中华田园猫，独立安静，适合上班族\n2. 小灰 - 14个月大的英短，沉稳友好，非常适合新手\n\n这两只猫咪都非常适合您的需求。要我帮您详细了解其中一只吗？', NULL, '2026-03-12 09:20:00'),
-- 会话3：大黄领养咨询
(3, 'USER', '我对金毛大黄很感兴趣', '{"source": "pet_profile", "pet_id": 3}', '2026-03-15 10:00:00'),
(3, 'ASSISTANT', '您好！大黄是一只3岁的金毛寻回犬，性格非常温顺友好。它已绝育已免疫，身体健康。大黄对人非常热情，是孩子的最佳玩伴。\n\n不过金毛属于大型犬，需要较大的活动空间。请问您家的居住环境是怎样的呢？', NULL, '2026-03-15 10:05:00'),
(3, 'USER', '我有个带院子的房子', NULL, '2026-03-15 10:10:00'),
(3, 'ASSISTANT', '太完美了！有院子的房子非常适合大黄。它可以在院子里活动和玩耍。金毛需要每天至少1-2小时的户外活动，您的时间安排如何呢？', NULL, '2026-03-15 10:15:00'),
(3, 'USER', '我退休了，每天时间很充裕', NULL, '2026-03-15 10:20:00'),
(3, 'ASSISTANT', '那简直太完美了！大黄遇到了最合适的领养人。您有充足的时间陪伴它，带它散步、玩耍。大黄一定会非常幸福！\n\n请问您之前有养过大型犬的经验吗？', NULL, '2026-03-15 10:25:00'),
(3, 'USER', '我以前养过德牧，很有经验', NULL, '2026-03-15 10:30:00'),
(3, 'ASSISTANT', '太棒了！有养大型犬经验的主人是大黄最好的归宿。我这就帮您创建领养申请，请保持电话畅通，救助站会尽快联系您确认家访时间。', NULL, '2026-03-15 10:35:00'),
-- 会话4：新手养猫建议
(4, 'USER', '我是第一次养宠物，猫好养吗？', NULL, '2026-03-14 16:00:00'),
(4, 'ASSISTANT', '您好！猫咪其实是很好的第一种宠物哦！相比狗狗，猫咪更独立，不需要每天遛它们，适合工作较忙的人。\n\n猫咪的优点：\n- 独立性强，不需要太多陪伴\n- 爱干净，会自己使用猫砂\n- 安静，不打扰邻居\n- 寿命较长，能陪伴您很久\n\n不过养猫也需要注意：\n- 定期铲屎，保持猫砂清洁\n- 提供猫抓板，防止家具被抓坏\n- 每天陪它玩一会儿，保持身心健康\n- 定期体检和疫苗接种\n\n请问您是住公寓还是house呢？', NULL, '2026-03-14 16:05:00'),
(4, 'USER', '我住公寓', NULL, '2026-03-14 16:10:00'),
(4, 'ASSISTANT', '公寓很适合养猫！猫咪不需要很大的空间，公寓大小刚刚好。建议您选择一只性格安静、易饲养的猫咪。\n\n我推荐您考虑小灰——一只14个月大的英短弟弟。它性格沉稳安静，不挑食，非常适合新手铲屎官！', NULL, '2026-03-14 16:15:00'),
(4, 'USER', '好的，我去了解一下', NULL, '2026-03-14 16:20:00'),
(4, 'ASSISTANT', '好的！我帮您查了小灰的档案。它身体健康，已绝育已免疫，性格非常好。您可以通过我们的平台提交领养申请，会有专人为您服务。\n\n祝您早日找到心仪的猫咪！如果有任何养猫问题，随时可以问我。', NULL, '2026-03-14 16:25:00');

-- MQ消息追踪表测试数据
INSERT INTO mq_message_log (message_id, business_type, payload, status, retry_count, error_message, created_at, processed_at) VALUES
('msg-20260320-001', 'ADOPTION_APPLICATION', '{"application_id": 1, "adopter_id": 1, "pet_id": 2, "action": "SUBMIT"}', 'PROCESSED', 0, NULL, '2026-03-20 10:30:00', '2026-03-20 10:30:05'),
('msg-20260320-002', 'ADOPTION_APPLICATION', '{"application_id": 1, "adopter_id": 1, "pet_id": 2, "action": "APPROVE"}', 'PROCESSED', 0, NULL, '2026-03-20 10:35:00', '2026-03-20 10:35:03'),
('msg-20260318-001', 'ADOPTION_APPLICATION', '{"application_id": 3, "adopter_id": 2, "pet_id": 1, "action": "SUBMIT"}', 'PROCESSED', 0, NULL, '2026-03-18 15:00:00', '2026-03-18 15:00:04'),
('msg-20260318-002', 'ADOPTION_APPLICATION', '{"application_id": 3, "adopter_id": 2, "pet_id": 1, "action": "APPROVE"}', 'PROCESSED', 0, NULL, '2026-03-18 15:05:00', '2026-03-18 15:05:06'),
('msg-20260324-001', 'PET_STATUS_UPDATE', '{"pet_id": 8, "old_status": "AVAILABLE", "new_status": "ADOPTED", "application_id": 7}', 'PROCESSED', 0, NULL, '2026-03-24 16:00:00', '2026-03-24 16:00:08'),
('msg-20260325-001', 'FOLLOW_UP_REMINDER', '{"application_id": 1, "days_after_adoption": 7, "scheduled_date": "2026-03-29"}', 'PROCESSED', 0, NULL, '2026-03-22 09:00:00', '2026-03-22 09:00:02'),
('msg-20260322-001', 'ADOPTION_NOTIFICATION', '{"user_id": 1, "pet_id": 2, "notification_type": "APPLICATION_APPROVED"}', 'PROCESSED', 0, NULL, '2026-03-22 14:00:00', '2026-03-22 14:00:03'),
('msg-20260326-001', 'KNOWLEDGE_RAG', '{"category": "BREED_CARE", "document_id": 1, "action": "EMBED"}', 'PENDING', 0, NULL, '2026-03-26 10:00:00', NULL),
('msg-20260326-002', 'KNOWLEDGE_RAG', '{"category": "HEALTH", "document_id": 2, "action": "EMBED"}', 'PROCESSING', 1, NULL, '2026-03-26 10:05:00', NULL);
