import { useState, useEffect } from 'react'
import { message, Spin, Button } from 'antd'
import { adopterApi, AdopterProfileRequest } from '../api/adopter'
import { useAuthStore } from '../stores/authStore'

export default function MyProfile() {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  
  const [form, setForm] = useState<AdopterProfileRequest>({
    housingType: '公寓',
    hasChildren: false,
    hasElderly: false,
    hasOtherPets: false,
    petExperience: '有一些经验',
    dailyHoursAvailable: 3,
    preferredPetSize: ['小型', '中型'],
    preferredPetAge: ['成年'],
    allergyInfo: '',
    activityLevel: '适量活动',
    adoptionMotivation: '',
  })

  useEffect(() => {
    if (isAuthenticated) {
      loadProfile()
    } else {
      setLoading(false)
    }
  }, [isAuthenticated])

  const loadProfile = async () => {
    setLoading(true)
    
    const response = await adopterApi.getProfile().catch(() => null)

    setLoading(false)

    if (response?.data) {
      setForm({
        housingType: response.data.housingType || '公寓',
        hasChildren: response.data.hasChildren || false,
        hasElderly: response.data.hasElderly || false,
        hasOtherPets: response.data.hasOtherPets || false,
        petExperience: response.data.petExperience || '有一些经验',
        dailyHoursAvailable: response.data.dailyHoursAvailable || 3,
        preferredPetSize: response.data.preferredPetSize || ['小型', '中型'],
        preferredPetAge: response.data.preferredPetAge || ['成年'],
        allergyInfo: response.data.allergyInfo || '',
        activityLevel: response.data.activityLevel || '适量活动',
        adoptionMotivation: response.data.adoptionMotivation || '',
      })
    }
  }

  const handleSave = async () => {
    if (!isAuthenticated) {
      message.warning('请先登录')
      return
    }

    setSaving(true)
    
    const response = await adopterApi.updateProfile(form).catch((err) => {
      message.error(err.message || '保存失败')
      return null
    })

    setSaving(false)

    if (response?.data) {
      message.success('画像已保存！')
    }
  }

  const handleDraft = () => {
    message.info('草稿已保存')
  }

  const updateForm = (key: keyof AdopterProfileRequest, value: string | number | boolean | string[]) => {
    setForm((prev) => ({ ...prev, [key]: value }))
  }

  const toggleArrayValue = (key: keyof AdopterProfileRequest, value: string) => {
    setForm((prev) => {
      const arr = prev[key] as string[]
      const newArr = arr.includes(value) ? arr.filter((v) => v !== value) : [...arr, value]
      return { ...prev, [key]: newArr }
    })
  }

  const housingTypes = ['🏠 公寓', '🏡 独栋/别墅', '🏢 宿舍/合租']
  const activityLevels = ['🧘 久坐型', '🚶 适量活动', '🏃 活跃型']
  const petSizeOptions = ['小型', '中型', '大型']
  const petAgeOptions = ['幼年', '成年', '老年']
  const experienceOptions = ['新手', '有一些经验', '经验丰富']

  const previewTags = [
    form.housingType.replace(/[^\s]+\s/, '') + '居住',
    !form.hasChildren && !form.hasElderly ? '无小孩/老人' : '',
    `每日${form.dailyHoursAvailable}h+`,
    form.activityLevel.replace(/[^\s]+\s/, ''),
    form.preferredPetSize.length > 0 ? form.preferredPetSize.join('、') + '型' : '',
  ].filter(Boolean)

  if (loading) {
    return (
      <div style={{ background: '#FFF9F5', minHeight: 'calc(100vh - 80px)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <Spin size="large" />
      </div>
    )
  }

  if (!isAuthenticated) {
    return (
      <div style={{ background: '#FFF9F5', minHeight: 'calc(100vh - 80px)', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
        <div style={{ fontSize: '80px', marginBottom: '20px' }}>🔐</div>
        <div style={{ fontSize: '20px', color: '#4A4A4A', marginBottom: '12px' }}>请先登录</div>
        <p style={{ color: '#999', marginBottom: '24px' }}>登录后即可设置你的领养画像</p>
        <Button onClick={() => window.location.href = '/login'} type="primary">
          去登录
        </Button>
      </div>
    )
  }

  return (
    <div style={{ background: '#FFF9F5', minHeight: 'calc(100vh - 80px)', padding: '40px' }}>
      <div style={{ maxWidth: '800px', margin: '0 auto' }}>
        <h1 style={{ fontSize: '32px', fontWeight: 700, color: '#4A4A4A', marginBottom: '8px' }}>我的领养画像</h1>
        <p style={{ color: '#999', fontSize: '15px', marginBottom: '40px' }}>完善你的画像，帮助 AI 更精准地为你推荐合适的宠物</p>

        {/* 居住环境 */}
        <div style={{ background: '#fff', borderRadius: '24px', padding: '32px', boxShadow: '0 4px 20px rgba(0,0,0,0.06)', marginBottom: '24px' }}>
          <div style={{ fontSize: '18px', fontWeight: 600, color: '#4A4A4A', marginBottom: '24px', display: 'flex', alignItems: 'center', gap: '12px' }}>
            <div style={{ width: '40px', height: '40px', background: 'linear-gradient(135deg, #FFE5CC, #FFD4A8)', borderRadius: '12px', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '20px' }}>
              🏠
            </div>
            居住环境
          </div>

          <div style={{ marginBottom: '24px' }}>
            <label style={{ fontSize: '14px', color: '#666', fontWeight: 500, marginBottom: '12px', display: 'block' }}>
              住房类型 <span style={{ color: '#FF8C42' }}>*</span>
            </label>
            <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
              {housingTypes.map((type) => (
                <button
                  key={type}
                  onClick={() => updateForm('housingType', type)}
                  style={{
                    padding: '10px 20px',
                    borderRadius: '50px',
                    fontSize: '14px',
                    fontWeight: 500,
                    cursor: 'pointer',
                    border: 'none',
                    background: form.housingType === type ? '#FF8C42' : '#FFF9F5',
                    color: form.housingType === type ? '#fff' : '#666',
                    transition: 'all 0.3s'
                  }}
                >
                  {type}
                </button>
              ))}
            </div>
          </div>
        </div>

        {/* 家庭情况 */}
        <div style={{ background: '#fff', borderRadius: '24px', padding: '32px', boxShadow: '0 4px 20px rgba(0,0,0,0.06)', marginBottom: '24px' }}>
          <div style={{ fontSize: '18px', fontWeight: 600, color: '#4A4A4A', marginBottom: '24px', display: 'flex', alignItems: 'center', gap: '12px' }}>
            <div style={{ width: '40px', height: '40px', background: 'linear-gradient(135deg, #FFE5CC, #FFD4A8)', borderRadius: '12px', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '20px' }}>
              👨‍👩‍👧
            </div>
            家庭情况
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '24px' }}>
            {[
              { key: 'hasChildren', label: '家中是否有小孩' },
              { key: 'hasElderly', label: '家中是否有老人' },
              { key: 'hasOtherPets', label: '家中是否有其他宠物' },
            ].map((item) => (
              <div key={item.key}>
                <label style={{ fontSize: '14px', color: '#666', fontWeight: 500, marginBottom: '12px', display: 'block' }}>{item.label}</label>
                <div style={{ display: 'flex', gap: '12px' }}>
                  {['有', '没有'].map((option) => (
                    <button
                      key={option}
                      onClick={() => updateForm(item.key as keyof AdopterProfileRequest, option === '有')}
                      style={{
                        padding: '10px 24px',
                        borderRadius: '50px',
                        fontSize: '14px',
                        fontWeight: 500,
                        cursor: 'pointer',
                        border: 'none',
                        background: form[item.key as keyof AdopterProfileRequest] === (option === '有') ? '#FF8C42' : '#FFF9F5',
                        color: form[item.key as keyof AdopterProfileRequest] === (option === '有') ? '#fff' : '#666',
                        transition: 'all 0.3s'
                      }}
                    >
                      {option}
                    </button>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* 时间与精力 */}
        <div style={{ background: '#fff', borderRadius: '24px', padding: '32px', boxShadow: '0 4px 20px rgba(0,0,0,0.06)', marginBottom: '24px' }}>
          <div style={{ fontSize: '18px', fontWeight: 600, color: '#4A4A4A', marginBottom: '24px', display: 'flex', alignItems: 'center', gap: '12px' }}>
            <div style={{ width: '40px', height: '40px', background: 'linear-gradient(135deg, #FFE5CC, #FFD4A8)', borderRadius: '12px', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '20px' }}>
              ⏰
            </div>
            时间与精力
          </div>

          <div style={{ marginBottom: '24px' }}>
            <label style={{ fontSize: '14px', color: '#666', fontWeight: 500, marginBottom: '12px', display: 'block' }}>每日可陪伴宠物的时间</label>
            <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
              <input
                type="range"
                min="0"
                max="12"
                value={form.dailyHoursAvailable}
                onChange={(e) => updateForm('dailyHoursAvailable', parseInt(e.target.value))}
                style={{ flex: 1, height: '8px', background: '#F5F5F5', borderRadius: '4px', outline: 'none' }}
              />
              <span style={{ fontSize: '18px', fontWeight: 600, color: '#FF8C42', minWidth: '100px' }}>
                {form.dailyHoursAvailable} 小时/天
              </span>
            </div>
          </div>

          <div>
            <label style={{ fontSize: '14px', color: '#666', fontWeight: 500, marginBottom: '12px', display: 'block' }}>您的活动水平</label>
            <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
              {activityLevels.map((level) => (
                <button
                  key={level}
                  onClick={() => updateForm('activityLevel', level)}
                  style={{
                    padding: '10px 20px',
                    borderRadius: '50px',
                    fontSize: '14px',
                    fontWeight: 500,
                    cursor: 'pointer',
                    border: 'none',
                    background: form.activityLevel === level ? '#FF8C42' : '#FFF9F5',
                    color: form.activityLevel === level ? '#fff' : '#666',
                    transition: 'all 0.3s'
                  }}
                >
                  {level}
                </button>
              ))}
            </div>
          </div>
        </div>

        {/* 养宠偏好 */}
        <div style={{ background: '#fff', borderRadius: '24px', padding: '32px', boxShadow: '0 4px 20px rgba(0,0,0,0.06)', marginBottom: '24px' }}>
          <div style={{ fontSize: '18px', fontWeight: 600, color: '#4A4A4A', marginBottom: '24px', display: 'flex', alignItems: 'center', gap: '12px' }}>
            <div style={{ width: '40px', height: '40px', background: 'linear-gradient(135deg, #FFE5CC, #FFD4A8)', borderRadius: '12px', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '20px' }}>
              🐾
            </div>
            养宠偏好
          </div>

          <div style={{ marginBottom: '24px' }}>
            <label style={{ fontSize: '14px', color: '#666', fontWeight: 500, marginBottom: '12px', display: 'block' }}>偏好的宠物体型（可多选）</label>
            <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
              {petSizeOptions.map((size) => (
                <button
                  key={size}
                  onClick={() => toggleArrayValue('preferredPetSize', size)}
                  style={{
                    padding: '10px 20px',
                    borderRadius: '50px',
                    fontSize: '14px',
                    fontWeight: 500,
                    cursor: 'pointer',
                    border: form.preferredPetSize.includes(size) ? '2px solid #FF8C42' : '2px solid transparent',
                    background: form.preferredPetSize.includes(size) ? '#FFE5CC' : '#FFF9F5',
                    color: form.preferredPetSize.includes(size) ? '#FF8C42' : '#666',
                    transition: 'all 0.3s'
                  }}
                >
                  {size}
                </button>
              ))}
            </div>
          </div>

          <div style={{ marginBottom: '24px' }}>
            <label style={{ fontSize: '14px', color: '#666', fontWeight: 500, marginBottom: '12px', display: 'block' }}>偏好的宠物年龄段（可多选）</label>
            <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
              {petAgeOptions.map((age) => (
                <button
                  key={age}
                  onClick={() => toggleArrayValue('preferredPetAge', age)}
                  style={{
                    padding: '10px 20px',
                    borderRadius: '50px',
                    fontSize: '14px',
                    fontWeight: 500,
                    cursor: 'pointer',
                    border: form.preferredPetAge.includes(age) ? '2px solid #FF8C42' : '2px solid transparent',
                    background: form.preferredPetAge.includes(age) ? '#FFE5CC' : '#FFF9F5',
                    color: form.preferredPetAge.includes(age) ? '#FF8C42' : '#666',
                    transition: 'all 0.3s'
                  }}
                >
                  {age}
                </button>
              ))}
            </div>
          </div>

          <div style={{ marginBottom: '24px' }}>
            <label style={{ fontSize: '14px', color: '#666', fontWeight: 500, marginBottom: '12px', display: 'block' }}>养宠经验</label>
            <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
              {experienceOptions.map((exp) => (
                <button
                  key={exp}
                  onClick={() => updateForm('petExperience', exp)}
                  style={{
                    padding: '10px 20px',
                    borderRadius: '50px',
                    fontSize: '14px',
                    fontWeight: 500,
                    cursor: 'pointer',
                    border: 'none',
                    background: form.petExperience === exp ? '#FF8C42' : '#FFF9F5',
                    color: form.petExperience === exp ? '#fff' : '#666',
                    transition: 'all 0.3s'
                  }}
                >
                  {exp}
                </button>
              ))}
            </div>
          </div>

          <div style={{ marginBottom: '24px' }}>
            <label style={{ fontSize: '14px', color: '#666', fontWeight: 500, marginBottom: '12px', display: 'block' }}>过敏信息（如有）</label>
            <input
              type="text"
              value={form.allergyInfo}
              onChange={(e) => updateForm('allergyInfo', e.target.value)}
              placeholder="如：对猫毛过敏"
              style={{
                width: '100%',
                padding: '14px 18px',
                border: '2px solid #FFE5CC',
                borderRadius: '12px',
                fontSize: '14px',
                background: '#FFF9F5',
                outline: 'none'
              }}
            />
          </div>

          <div>
            <label style={{ fontSize: '14px', color: '#666', fontWeight: 500, marginBottom: '12px', display: 'block' }}>领养动机（选填）</label>
            <textarea
              value={form.adoptionMotivation}
              onChange={(e) => updateForm('adoptionMotivation', e.target.value)}
              placeholder="为什么想要领养一只宠物？"
              rows={4}
              style={{
                width: '100%',
                padding: '14px 18px',
                border: '2px solid #FFE5CC',
                borderRadius: '12px',
                fontSize: '14px',
                background: '#FFF9F5',
                outline: 'none',
                resize: 'none',
                fontFamily: 'inherit'
              }}
            />
          </div>
        </div>

        {/* AI 画像预览 */}
        <div style={{ background: 'linear-gradient(135deg, #FF8C42, #FF6B35)', borderRadius: '24px', padding: '32px', textAlign: 'center', color: '#fff', marginBottom: '24px' }}>
          <div style={{ fontSize: '14px', opacity: 0.9, marginBottom: '8px' }}>您的 AI 画像预览</div>
          <div style={{ fontSize: '28px', fontWeight: 700, marginBottom: '16px' }}>爱心领养人</div>
          <div style={{ display: 'flex', justifyContent: 'center', gap: '8px', flexWrap: 'wrap' }}>
            {previewTags.map((tag, index) => (
              <span key={index} style={{ padding: '6px 14px', background: 'rgba(255,255,255,0.2)', borderRadius: '50px', fontSize: '13px' }}>
                {tag}
              </span>
            ))}
          </div>
        </div>

        {/* 按钮 */}
        <div style={{ display: 'flex', gap: '16px', justifyContent: 'center', marginBottom: '24px' }}>
          <button
            onClick={handleDraft}
            style={{
              padding: '14px 32px',
              borderRadius: '50px',
              fontSize: '15px',
              fontWeight: 600,
              cursor: 'pointer',
              border: '2px solid #FF8C42',
              background: '#fff',
              color: '#FF8C42',
              transition: 'all 0.3s'
            }}
          >
            保存草稿
          </button>
          <button
            onClick={handleSave}
            disabled={saving}
            style={{
              padding: '14px 32px',
              borderRadius: '50px',
              fontSize: '15px',
              fontWeight: 600,
              cursor: saving ? 'not-allowed' : 'pointer',
              border: 'none',
              background: 'linear-gradient(135deg, #FF8C42, #FF6B35)',
              color: '#fff',
              boxShadow: '0 4px 16px rgba(255,107,53,0.3)',
              transition: 'all 0.3s',
              opacity: saving ? 0.7 : 1
            }}
          >
            {saving ? '保存中...' : '保存并生成向量 ➤'}
          </button>
        </div>
      </div>
    </div>
  )
}
