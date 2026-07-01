import { useState, useEffect } from 'react'
import { Button, Spin } from 'antd'
import { useNavigate } from 'react-router-dom'
import {
  AimOutlined,
  BookOutlined,
  HeartOutlined,
  MedicineBoxOutlined,
} from '@ant-design/icons'
import { petApi, PetProfile } from '../api/pet'

export default function Home() {
  const navigate = useNavigate()
  const [pets, setPets] = useState<PetProfile[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    loadRecommendedPets()
  }, [])

  const loadRecommendedPets = async () => {
    setLoading(true)
    setError(null)
    
    const response = await petApi.getRecommended(7).catch((err) => {
      setError(err.message || '加载失败，请稍后重试')
      return null
    })

    setLoading(false)

    if (response?.data) {
      setPets(response.data)
    }
  }

  const getPetEmoji = (species: string) => {
    switch (species?.toLowerCase()) {
      case 'dog':
      case '狗狗':
        return '🐕'
      case 'cat':
      case '猫咪':
        return '🐱'
      case 'rabbit':
      case '兔子':
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
      case '小型':
        return '小型'
      case 'medium':
      case '中型':
        return '中型'
      case 'large':
      case '大型':
        return '大型'
      default:
        return size || '中型'
    }
  }

  const features = [
    { icon: <AimOutlined style={{ fontSize: '48px' }} />, title: 'AI 智能匹配', desc: '根据你的生活方式、居住环境，AI 精准推荐最适合你的宠物伙伴' },
    { icon: <BookOutlined style={{ fontSize: '48px' }} />, title: '领养知识库', desc: '从领养流程到日常护理，AI 助手 24 小时为你解答养宠问题' },
    { icon: <HeartOutlined style={{ fontSize: '48px' }} />, title: '全程陪伴', desc: '从匹配到领养，再到后续回访，我们一直守护这份缘分' },
    { icon: <MedicineBoxOutlined style={{ fontSize: '48px' }} />, title: '真实可靠', desc: '所有宠物都经过救助站认证，健康、性格、年龄等信息真实可查' },
  ]

  return (
    <div style={{ background: '#FFF9F5', minHeight: '100vh' }}>
      {/* Hero Section */}
      <section 
        style={{ 
          background: 'linear-gradient(180deg, #FFE5CC 0%, #FFF5EB 100%)',
          padding: '100px 60px',
          textAlign: 'center',
          position: 'relative',
          overflow: 'hidden'
        }}
      >
        <div 
          style={{ 
            position: 'absolute',
            top: '-20px', 
            right: '-20px', 
            width: '100px', 
            height: '100px',
            background: 'radial-gradient(circle, #FFE5CC 0%, transparent 70%)',
            opacity: 0.6 
          }} 
        />
        <div style={{ position: 'relative', zIndex: 1, maxWidth: '900px', margin: '0 auto' }}>
          <span 
            style={{ 
              display: 'inline-block',
              background: '#fff', 
              padding: '10px 24px', 
              borderRadius: '50px', 
              fontSize: '14px', 
              color: '#FF8C42',
              marginBottom: '32px',
              boxShadow: '0 4px 16px rgba(0,0,0,0.1)'
            }}
          >
            ✨ 领养平台
          </span>
          <h1 
            style={{ 
              fontSize: '64px', 
              fontWeight: 700, 
              color: '#4A4A4A',
              marginBottom: '20px',
              lineHeight: 1.2,
              textShadow: '0 2px 8px rgba(255,255,255,0.8)'
            }}
          >
            每一次相遇，<span style={{ color: '#FF8C42' }}>都是最好的安排</span>
          </h1>
          <p 
            style={{ 
              fontSize: '20px', 
              color: 'rgba(74,74,74,0.9)', 
              marginBottom: '40px',
              lineHeight: 1.8
            }}
          >
            在这里，<span style={{ color: '#FF8C42', fontWeight: 600 }}>爱</span>会找到彼此。<br />
            通过 AI 智能匹配，我们帮你找到那个命中注定的毛孩子。
          </p>
          <div style={{ display: 'flex', gap: '20px', justifyContent: 'center', marginTop: '40px' }}>
            <Button
              type="primary"
              size="large"
              onClick={() => navigate('/pets')}
              style={{ 
                background: 'linear-gradient(135deg, #FF8C42, #FF6B35)', 
                padding: '16px 40px',
                borderRadius: '50px',
                color: '#fff',
                fontWeight: 600,
                fontSize: '16px',
                boxShadow: '0 8px 24px rgba(255,107,53,0.3)',
                height: 'auto',
                border: 'none'
              }}
            >
              探索宠物 🐱
            </Button>
            <Button
              size="large"
              style={{ 
                background: '#fff', 
                padding: '16px 40px',
                borderRadius: '50px',
                color: '#FF8C42',
                fontWeight: 600,
                fontSize: '16px',
                border: '2px solid #FF8C42',
                boxShadow: '0 2px 12px rgba(255,140,66,0.1)',
                height: 'auto'
              }}
            >
              了解更多 →
            </Button>
          </div>
        </div>
      </section>

      {/* Pet Showcase */}
      <section 
        style={{ 
          maxWidth: '1400px', 
          margin: '60px auto', 
          padding: '0 80px 80px'
        }}
      >
        <div style={{ textAlign: 'center', marginBottom: '60px' }}>
          <h2 
            style={{ 
              fontSize: '48px', 
              fontWeight: 700, 
              color: '#4A4A4A',
              marginBottom: '12px'
            }}
          >
            为你推荐 🔥
          </h2>
          <p style={{ fontSize: '18px', color: '#999' }}>基于你的偏好，AI 精选匹配</p>
        </div>

        {loading ? (
          <div style={{ textAlign: 'center', padding: '80px 0' }}>
            <Spin size="large" />
            <p style={{ color: '#999', marginTop: '16px' }}>正在加载推荐宠物...</p>
          </div>
        ) : error ? (
          <div style={{ textAlign: 'center', padding: '80px 0' }}>
            <div style={{ fontSize: '64px', marginBottom: '16px' }}>😢</div>
            <p style={{ color: '#999', marginBottom: '24px' }}>{error}</p>
            <Button onClick={loadRecommendedPets} type="primary">
              重新加载
            </Button>
          </div>
        ) : pets.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '80px 0' }}>
            <div style={{ fontSize: '64px', marginBottom: '16px' }}>🐾</div>
            <p style={{ color: '#999' }}>暂无推荐宠物，快去看看其他宠物吧！</p>
          </div>
        ) : (
          <div 
            style={{ 
              display: 'grid', 
              gridTemplateColumns: 'repeat(4, 1fr)', 
              gap: '32px' 
            }}
          >
            {pets.map((pet) => (
              <div
                key={pet.id}
                onClick={() => navigate(`/pets/${pet.id}`)}
                style={{ 
                  background: '#fff', 
                  borderRadius: '28px', 
                  overflow: 'hidden',
                  boxShadow: '0 6px 24px rgba(0,0,0,0.08)',
                  transition: 'all 0.4s',
                  cursor: 'pointer',
                  border: '2px solid transparent'
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.transform = 'translateY(-6px)'
                  e.currentTarget.style.borderColor = '#FF8C42'
                  e.currentTarget.style.boxShadow = '0 12px 40px rgba(255,140,66,0.15)'
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.transform = 'translateY(0)'
                  e.currentTarget.style.borderColor = 'transparent'
                  e.currentTarget.style.boxShadow = '0 6px 24px rgba(0,0,0,0.08)'
                }}
              >
                <div 
                  style={{ 
                    height: '220px', 
                    background: 'linear-gradient(135deg, #FFE5CC, #FFF5EB)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontSize: '100px',
                    position: 'relative',
                    overflow: 'hidden'
                  }}
                >
                  {getPetEmoji(pet.species)}
                  <div 
                    style={{ 
                      position: 'absolute',
                      top: '20px', 
                      right: '20px', 
                      width: '100px', 
                      height: '60px',
                      background: 'radial-gradient(circle, #fff 0%, transparent 50%)'
                    }} 
                  />
                </div>
                <div style={{ padding: '28px' }}>
                  <div 
                    style={{ 
                      fontSize: '24px', 
                      fontWeight: 600, 
                      color: '#4A4A4A', 
                      marginBottom: '8px' 
                    }}
                  >
                    {pet.name}
                  </div>
                  <div 
                    style={{ 
                      color: '#FF8C42', 
                      fontSize: '14px', 
                      fontWeight: 600, 
                      marginBottom: '12px' 
                    }}
                  >
                    {pet.breed} · {getAgeText(pet.ageMonths)} · {getSizeText(pet.size)}
                  </div>
                  <div 
                    style={{ 
                      color: '#999', 
                      fontSize: '14px', 
                      marginBottom: '20px',
                      lineHeight: 1.6
                    }}
                  >
                    {pet.description || '等待有缘人的毛孩子'}
                  </div>
                  <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
                    {pet.personalityTags?.slice(0, 3).map((tag, tagIndex) => (
                      <span
                        key={tagIndex}
                        style={{ 
                          background: 'linear-gradient(135deg, #FFE5CC, #FFD4A8)', 
                          color: '#fff', 
                          padding: '6px 14px', 
                          borderRadius: '50px', 
                          fontSize: '12px', 
                          fontWeight: 500 
                        }}
                      >
                        {tag}
                      </span>
                    ))}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </section>

      {/* Features */}
      <section 
        style={{ 
          background: '#fff', 
          maxWidth: '1400px', 
          margin: '0 auto 80px', 
          padding: '80px',
          borderRadius: '28px',
          boxShadow: '0 6px 24px rgba(0,0,0,0.08)'
        }}
      >
        <div style={{ textAlign: 'center', marginBottom: '40px' }}>
          <h2 
            style={{ 
              fontSize: '40px', 
              fontWeight: 700, 
              color: '#4A4A4A',
              marginBottom: '16px'
            }}
          >
            平台核心功能 💡
          </h2>
          <p style={{ fontSize: '18px', color: '#999' }}>让领养变得更简单、更智能</p>
        </div>

        <div 
          style={{ 
            display: 'grid', 
            gridTemplateColumns: 'repeat(4, 1fr)', 
            gap: '32px' 
          }}
        >
          {features.map((feature, index) => (
            <div
              key={index}
              style={{ 
                background: '#FFF5EB', 
                padding: '40px', 
                borderRadius: '20px', 
                textAlign: 'center',
                border: '2px solid #FFE5CC',
                transition: 'all 0.3s'
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.transform = 'translateY(-4px)'
                e.currentTarget.style.background = '#FFE5D4'
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.transform = 'translateY(0)'
                e.currentTarget.style.background = '#FFF5EB'
              }}
            >
              <div 
                style={{ 
                  width: '96px', 
                  height: '96px', 
                  background: 'linear-gradient(135deg, #FF8C42, #FFB347)', 
                  borderRadius: '24px', 
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  color: '#fff', 
                  margin: '0 auto 20px',
                  boxShadow: '0 6px 16px rgba(255,140,66,0.2)'
                }}
              >
                {feature.icon}
              </div>
              <h3 
                style={{ 
                  fontSize: '20px', 
                  color: '#4A4A4A', 
                  marginBottom: '12px',
                  marginTop: '20px',
                  fontWeight: 600
                }}
              >
                {feature.title}
              </h3>
              <p style={{ fontSize: '14px', color: '#999', lineHeight: 1.7 }}>
                {feature.desc}
              </p>
            </div>
          ))}
        </div>
      </section>
    </div>
  )
}
