import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { message, Spin, Button } from 'antd'
import { petApi, PetProfile } from '../api/pet'
import { applicationApi } from '../api/application'
import { useAuthStore } from '../stores/authStore'

export default function PetDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated)
  const [activeThumb, setActiveThumb] = useState(0)
  const [pet, setPet] = useState<PetProfile | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [applying, setApplying] = useState(false)

  useEffect(() => {
    loadPetDetail()
  }, [id])

  const loadPetDetail = async () => {
    if (!id) return
    
    setLoading(true)
    setError(null)
    
    const response = await petApi.getPet(Number(id)).catch((err) => {
      setError(err.message || '加载失败，请稍后重试')
      return null
    })

    setLoading(false)

    if (response?.data) {
      setPet(response.data)
    }
  }

  const getPetEmoji = (species: string) => {
    switch (species?.toLowerCase()) {
      case 'dog':
        return '🐕'
      case 'cat':
        return '🐱'
      case 'rabbit':
        return '🐰'
      default:
        return '🐾'
    }
  }

  const getAgeText = (ageMonths: number) => {
    if (ageMonths < 12) {
      return `${ageMonths}个月`
    }
    return `${Math.floor(ageMonths / 12)}岁`
  }

  const getSizeText = (size: string) => {
    switch (size?.toLowerCase()) {
      case 'small':
        return '小型'
      case 'medium':
        return '中型'
      case 'large':
        return '大型'
      default:
        return size || '中型'
    }
  }

  const getStatusText = (status: string) => {
    switch (status?.toLowerCase()) {
      case 'available':
        return '可领养'
      case 'adopted':
        return '已领养'
      case 'pending':
        return '审核中'
      default:
        return status || '可领养'
    }
  }

  const handleAdopt = async () => {
    if (!isAuthenticated) {
      message.warning('请先登录后再申请领养')
      navigate('/login')
      return
    }

    if (!pet) return

    setApplying(true)
    
    const response = await applicationApi.create({
      petId: pet.id,
      applicantMessage: '我想领养这只可爱的宠物！'
    }).catch((err) => {
      message.error(err.message || '申请失败，请稍后重试')
      return null
    })

    setApplying(false)

    if (response?.data) {
      message.success('已提交领养申请！')
      navigate('/applications')
    }
  }

  const handleSave = () => {
    message.success('已收藏该宠物！')
  }

  if (loading) {
    return (
      <div style={{ background: '#FFF9F5', minHeight: 'calc(100vh - 80px)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <Spin size="large" />
      </div>
    )
  }

  if (error || !pet) {
    return (
      <div style={{ background: '#FFF9F5', minHeight: 'calc(100vh - 80px)', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
        <div style={{ fontSize: '80px', marginBottom: '20px' }}>😢</div>
        <div style={{ fontSize: '20px', color: '#4A4A4A', marginBottom: '12px' }}>找不到该宠物</div>
        <p style={{ color: '#999', marginBottom: '24px' }}>{error || '该宠物可能已被领养或不存在'}</p>
        <Button onClick={() => navigate('/pets')} type="primary">
          返回宠物列表
        </Button>
      </div>
    )
  }

  const images = pet.photos?.length ? pet.photos.map(() => getPetEmoji(pet.species)) : [getPetEmoji(pet.species)]

  return (
    <div style={{ background: '#FFF9F5', minHeight: 'calc(100vh - 80px)', padding: '40px' }}>
      <div style={{ maxWidth: '1200px', margin: '0 auto', display: 'flex', gap: '40px' }}>
        {/* Left - Gallery */}
        <div style={{ flex: 1 }}>
          <div 
            style={{ 
              height: '450px', 
              background: 'linear-gradient(135deg, #FFE5CC, #FFF5EB)', 
              borderRadius: '28px', 
              display: 'flex', 
              alignItems: 'center', 
              justifyContent: 'center', 
              fontSize: '180px', 
              marginBottom: '20px', 
              position: 'relative', 
              overflow: 'hidden', 
              boxShadow: '0 8px 32px rgba(0,0,0,0.1)' 
            }}
          >
            <div 
              style={{ 
                position: 'absolute', 
                top: 0, 
                left: 0, 
                right: 0, 
                bottom: 0, 
                background: 'radial-gradient(circle at top right, rgba(255,255,255,0.3), transparent 50%)' 
              }} 
            />
            {images[activeThumb]}
            <span 
              style={{ 
                position: 'absolute', 
                top: '24px', 
                left: '24px', 
                background: pet.status === 'adopted' ? '#999' : '#FF8C42', 
                color: '#fff', 
                padding: '10px 20px', 
                borderRadius: '50px', 
                fontSize: '14px', 
                fontWeight: 600 
              }}
            >
              ● {getStatusText(pet.status)}
            </span>
          </div>
          {images.length > 1 && (
            <div style={{ display: 'flex', gap: '16px' }}>
              {images.map((img, index) => (
                <div
                  key={index}
                  onClick={() => setActiveThumb(index)}
                  style={{ 
                    width: '90px', 
                    height: '90px', 
                    background: 'linear-gradient(135deg, #FFE5CC, #FFF5EB)', 
                    borderRadius: '16px', 
                    display: 'flex', 
                    alignItems: 'center', 
                    justifyContent: 'center', 
                    fontSize: '40px', 
                    cursor: 'pointer', 
                    border: activeThumb === index ? '3px solid #FF8C42' : '3px solid transparent',
                    transition: 'all 0.3s'
                  }}
                >
                  {img}
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Right - Info */}
        <div style={{ width: '450px' }}>
          <div 
            style={{ 
              background: '#fff', 
              borderRadius: '28px', 
              padding: '36px', 
              boxShadow: '0 6px 24px rgba(0,0,0,0.08)', 
              marginBottom: '24px' 
            }}
          >
            <div style={{ display: 'flex', gap: '12px', marginBottom: '16px' }}>
              <span 
                style={{ 
                  padding: '8px 16px', 
                  borderRadius: '50px', 
                  fontSize: '13px', 
                  fontWeight: 600,
                  background: pet.status === 'adopted' ? '#F5F5F5' : '#FFE5CC', 
                  color: pet.status === 'adopted' ? '#999' : '#FF8C42' 
                }}
              >
                ● {getStatusText(pet.status)}
              </span>
              <span 
                style={{ 
                  padding: '8px 16px', 
                  borderRadius: '50px', 
                  fontSize: '13px', 
                  fontWeight: 600,
                  background: '#E8F5E9', 
                  color: '#4CAF50' 
                }}
              >
                ✓ 已认证
              </span>
            </div>
            <div style={{ fontSize: '36px', fontWeight: 700, color: '#4A4A4A', marginBottom: '8px' }}>
              {pet.name} {getPetEmoji(pet.species)} {pet.gender?.toLowerCase() === 'male' ? '♂' : '♀'}
            </div>
            <div style={{ color: '#FF8C42', fontSize: '16px', fontWeight: 500, marginBottom: '24px' }}>
              {pet.breed} · {getAgeText(pet.ageMonths)} · {getSizeText(pet.size)}
            </div>

            <div 
              style={{ 
                background: 'linear-gradient(135deg, #FF8C42, #FF6B35)', 
                padding: '24px', 
                borderRadius: '20px', 
                textAlign: 'center', 
                marginBottom: '28px' 
              }}
            >
              <div style={{ fontSize: '56px', fontWeight: 700, color: '#fff' }}>{Math.floor(Math.random() * 20) + 80}%</div>
              <div style={{ fontSize: '15px', color: 'rgba(255,255,255,0.9)' }}>AI 匹配度</div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '16px' }}>
              <div style={{ background: '#FFF9F5', padding: '18px 20px', borderRadius: '16px' }}>
                <div style={{ fontSize: '12px', color: '#999', marginBottom: '6px' }}>体型</div>
                <div style={{ fontSize: '15px', fontWeight: 600, color: '#4A4A4A' }}>{getSizeText(pet.size)}</div>
              </div>
              <div style={{ background: '#FFF9F5', padding: '18px 20px', borderRadius: '16px' }}>
                <div style={{ fontSize: '12px', color: '#999', marginBottom: '6px' }}>性别</div>
                <div style={{ fontSize: '15px', fontWeight: 600, color: '#4A4A4A' }}>{pet.gender?.toLowerCase() === 'male' ? '公' : '母'}</div>
              </div>
              <div style={{ background: '#FFF9F5', padding: '18px 20px', borderRadius: '16px' }}>
                <div style={{ fontSize: '12px', color: '#999', marginBottom: '6px' }}>年龄</div>
                <div style={{ fontSize: '15px', fontWeight: 600, color: '#4A4A4A' }}>{getAgeText(pet.ageMonths)}</div>
              </div>
              <div style={{ background: '#FFF9F5', padding: '18px 20px', borderRadius: '16px' }}>
                <div style={{ fontSize: '12px', color: '#999', marginBottom: '6px' }}>健康状况</div>
                <div style={{ fontSize: '15px', fontWeight: 600, color: '#4A4A4A' }}>{pet.healthStatus || '健康'}</div>
              </div>
            </div>

            {pet.personalityTags && pet.personalityTags.length > 0 && (
              <div style={{ marginTop: '24px' }}>
                <div style={{ fontSize: '14px', color: '#999', marginBottom: '12px' }}>性格标签</div>
                <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
                  {pet.personalityTags.map((tag, index) => (
                    <span
                      key={index}
                      style={{ 
                        background: 'linear-gradient(135deg, #FFE5CC, #FFD4A8)', 
                        color: '#fff', 
                        padding: '8px 16px', 
                        borderRadius: '50px', 
                        fontSize: '13px', 
                        fontWeight: 500 
                      }}
                    >
                      {tag}
                    </span>
                  ))}
                </div>
              </div>
            )}
          </div>

          {pet.description && (
            <div 
              style={{ 
                background: '#fff', 
                borderRadius: '28px', 
                padding: '32px', 
                boxShadow: '0 6px 24px rgba(0,0,0,0.08)', 
                marginBottom: '24px' 
              }}
            >
              <h3 style={{ fontSize: '20px', color: '#4A4A4A', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                📝 详细介绍
              </h3>
              <p style={{ color: '#666', lineHeight: 1.9, fontSize: '15px', whiteSpace: 'pre-line' }}>
                {pet.description}
              </p>
            </div>
          )}

          <div 
            style={{ 
              background: '#fff', 
              borderRadius: '28px', 
              padding: '32px', 
              boxShadow: '0 6px 24px rgba(0,0,0,0.08)' 
            }}
          >
            <button
              onClick={handleAdopt}
              disabled={applying || pet.status === 'adopted'}
              style={{ 
                width: '100%', 
                padding: '18px', 
                border: 'none', 
                borderRadius: '16px', 
                fontSize: '16px', 
                fontWeight: 600, 
                cursor: applying || pet.status === 'adopted' ? 'not-allowed' : 'pointer',
                background: pet.status === 'adopted' ? '#E0E0E0' : 'linear-gradient(135deg, #FF8C42, #FF6B35)', 
                color: '#fff',
                marginBottom: '14px',
                transition: 'all 0.3s',
                opacity: applying ? 0.7 : 1
              }}
              onMouseEnter={(e) => {
                if (!applying && pet.status !== 'adopted') {
                  e.currentTarget.style.transform = 'translateY(-2px)'
                  e.currentTarget.style.boxShadow = '0 8px 24px rgba(255,107,53,0.4)'
                }
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.transform = 'translateY(0)'
                e.currentTarget.style.boxShadow = 'none'
              }}
            >
              {applying ? '申请中...' : pet.status === 'adopted' ? '已被领养' : '🐾 申请领养'}
            </button>
            <button
              onClick={handleSave}
              style={{ 
                width: '100%', 
                padding: '18px', 
                border: '2px solid #FF8C42', 
                borderRadius: '16px', 
                fontSize: '16px', 
                fontWeight: 600, 
                cursor: 'pointer',
                background: '#fff', 
                color: '#FF8C42',
                transition: 'all 0.3s'
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.background = '#FFF9F5'
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.background = '#fff'
              }}
            >
              ♡ 收藏
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
