import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { Spin, Button } from 'antd'
import { petApi, PetProfile, PetQueryRequest, PageResult } from '../api/pet'

export default function PetList() {
  const navigate = useNavigate()
  
  const [selectedType, setSelectedType] = useState('全部')
  const [selectedSize, setSelectedSize] = useState('中型')
  const [selectedAge, setSelectedAge] = useState('成年')
  const [selectedGender, setSelectedGender] = useState('不限')
  const [selectedHealth, setSelectedHealth] = useState('全部')
  
  const [pets, setPets] = useState<PetProfile[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [pageResult, setPageResult] = useState<PageResult<PetProfile> | null>(null)
  const [currentPage, setCurrentPage] = useState(1)
  const pageSize = 9

  useEffect(() => {
    loadPets()
  }, [selectedType, selectedSize, selectedAge, selectedGender, selectedHealth, currentPage])

  const loadPets = async () => {
    setLoading(true)
    setError(null)
    
    const params: PetQueryRequest = {
      page: currentPage,
      pageSize: pageSize,
    }

    if (selectedType !== '全部') {
      const typeMap: Record<string, string> = {
        '🐕 狗狗': 'dog',
        '🐱 猫咪': 'cat',
        '🐰 其他': 'other',
      }
      params.species = typeMap[selectedType]
    }

    if (selectedSize !== '中型') {
      const sizeMap: Record<string, string> = {
        '小型': 'small',
        '中型': 'medium',
        '大型': 'large',
      }
      params.size = sizeMap[selectedSize]
    }

    if (selectedGender !== '不限') {
      params.gender = selectedGender === '♂ 公' ? 'male' : 'female'
    }

    const response = await petApi.queryPets(params).catch((err) => {
      setError(err.message || '加载失败，请稍后重试')
      return null
    })

    setLoading(false)

    if (response?.data) {
      setPets(response.data.records)
      setPageResult(response.data)
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

  const getGenderIcon = (gender: string) => {
    return gender?.toLowerCase() === 'male' ? '♂' : '♀'
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

  const filterGroups = [
    {
      label: '物种',
      options: ['全部', '🐕 狗狗', '🐱 猫咪', '🐰 其他'],
      selected: selectedType,
      onSelect: setSelectedType
    },
    {
      label: '体型',
      options: ['小型', '中型', '大型'],
      selected: selectedSize,
      onSelect: setSelectedSize
    },
    {
      label: '年龄',
      options: ['幼年', '成年', '老年'],
      selected: selectedAge,
      onSelect: setSelectedAge
    },
    {
      label: '性别',
      options: ['不限', '♂ 公', '♀ 母'],
      selected: selectedGender,
      onSelect: setSelectedGender
    },
    {
      label: '健康状况',
      options: ['全部', '✓ 已绝育', '✓ 已疫苗', '✓ 已驱虫'],
      selected: selectedHealth,
      onSelect: setSelectedHealth
    }
  ]

  const handleReset = () => {
    setSelectedType('全部')
    setSelectedSize('中型')
    setSelectedAge('成年')
    setSelectedGender('不限')
    setSelectedHealth('全部')
    setCurrentPage(1)
  }

  const totalPages = pageResult ? Math.ceil(pageResult.total / pageSize) : 0

  return (
    <div style={{ background: '#FFF9F5', minHeight: 'calc(100vh - 80px)', padding: '40px' }}>
      <div 
        style={{ 
          maxWidth: '1400px', 
          margin: '0 auto', 
          display: 'flex', 
          gap: '40px' 
        }}
      >
        {/* Sidebar */}
        <aside style={{ width: '300px', flexShrink: 0 }}>
          <div 
            style={{ 
              background: '#fff', 
              borderRadius: '24px', 
              padding: '32px', 
              boxShadow: '0 4px 20px rgba(0,0,0,0.06)' 
            }}
          >
            <div 
              style={{ 
                fontSize: '20px', 
                fontWeight: 600, 
                color: '#4A4A4A', 
                marginBottom: '28px',
                display: 'flex',
                alignItems: 'center',
                gap: '8px'
              }}
            >
              🎛️ 筛选条件
            </div>
            
            {filterGroups.map((group, groupIndex) => (
              <div key={groupIndex} style={{ marginBottom: '28px' }}>
                <div 
                  style={{ 
                    fontSize: '14px', 
                    color: '#999', 
                    marginBottom: '14px',
                    fontWeight: 500
                  }}
                >
                  {group.label}
                </div>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '10px' }}>
                  {group.options.map((option) => (
                    <button
                      key={option}
                      onClick={() => {
                        group.onSelect(option)
                        setCurrentPage(1)
                      }}
                      style={{ 
                        padding: '10px 18px', 
                        background: group.selected === option ? '#FF8C42' : '#FFF5EB', 
                        borderRadius: '50px', 
                        fontSize: '14px', 
                        cursor: 'pointer',
                        border: group.selected === option ? '2px solid #FF8C42' : '2px solid transparent',
                        transition: 'all 0.3s',
                        color: group.selected === option ? '#fff' : '#666'
                      }}
                    >
                      {option}
                    </button>
                  ))}
                </div>
              </div>
            ))}
            
            <button 
              onClick={handleReset}
              style={{ 
                width: '100%', 
                padding: '14px', 
                background: '#fff', 
                border: '2px solid #FFE5CC', 
                borderRadius: '50px', 
                color: '#FF8C42', 
                fontSize: '14px', 
                fontWeight: 600, 
                cursor: 'pointer',
                transition: 'all 0.3s',
                marginTop: '20px'
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.background = '#FFF5EB'
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.background = '#fff'
              }}
            >
              🔄 重置筛选
            </button>
          </div>
        </aside>

        {/* Main Content */}
        <main style={{ flex: 1 }}>
          {/* List Header */}
          <div 
            style={{ 
              display: 'flex', 
              justifyContent: 'space-between', 
              alignItems: 'center', 
              marginBottom: '32px', 
              background: '#fff', 
              padding: '24px 32px', 
              borderRadius: '20px', 
              boxShadow: '0 4px 20px rgba(0,0,0,0.06)' 
            }}
          >
            <div style={{ fontSize: '15px', color: '#666' }}>
              共找到 <strong style={{ color: '#FF8C42', fontSize: '18px' }}>{pageResult?.total || 0}</strong> 只符合条件的宠物
            </div>
            <select 
              style={{ 
                padding: '12px 20px', 
                border: '2px solid #FFE5CC', 
                borderRadius: '50px', 
                fontSize: '14px', 
                background: '#fff', 
                color: '#666', 
                cursor: 'pointer',
                outline: 'none'
              }}
            >
              <option>智能排序（推荐最高）</option>
              <option>最新发布</option>
              <option>匹配度最高</option>
              <option>年龄从小到大</option>
            </select>
          </div>

          {/* Loading / Error / Pet Grid */}
          {loading ? (
            <div style={{ textAlign: 'center', padding: '80px 0' }}>
              <Spin size="large" />
              <p style={{ color: '#999', marginTop: '16px' }}>正在加载宠物列表...</p>
            </div>
          ) : error ? (
            <div style={{ textAlign: 'center', padding: '80px 0', background: '#fff', borderRadius: '24px' }}>
              <div style={{ fontSize: '64px', marginBottom: '16px' }}>😢</div>
              <p style={{ color: '#999', marginBottom: '24px' }}>{error}</p>
              <Button onClick={loadPets} type="primary">
                重新加载
              </Button>
            </div>
          ) : pets.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '80px 0', background: '#fff', borderRadius: '24px' }}>
              <div style={{ fontSize: '80px', marginBottom: '20px' }}>🐾</div>
              <div style={{ fontSize: '20px', fontWeight: 600, color: '#4A4A4A', marginBottom: '12px' }}>暂无符合条件的宠物</div>
              <div style={{ color: '#999', fontSize: '15px', marginBottom: '24px' }}>试试调整筛选条件吧！</div>
              <Button onClick={handleReset} type="primary">
                重置筛选
              </Button>
            </div>
          ) : (
            <>
              <div 
                style={{ 
                  display: 'grid', 
                  gridTemplateColumns: 'repeat(3, 1fr)', 
                  gap: '28px' 
                }}
              >
                {pets.map((pet) => (
                  <div
                    key={pet.id}
                    onClick={() => navigate(`/pets/${pet.id}`)}
                    style={{ 
                      background: '#fff', 
                      borderRadius: '24px', 
                      overflow: 'hidden',
                      boxShadow: '0 4px 20px rgba(0,0,0,0.06)',
                      transition: 'all 0.4s',
                      cursor: 'pointer',
                      border: '2px solid transparent'
                    }}
                    onMouseEnter={(e) => {
                      e.currentTarget.style.transform = 'translateY(-6px)'
                      e.currentTarget.style.borderColor = '#FFE5CC'
                      e.currentTarget.style.boxShadow = '0 12px 40px rgba(255,140,66,0.15)'
                    }}
                    onMouseLeave={(e) => {
                      e.currentTarget.style.transform = 'translateY(0)'
                      e.currentTarget.style.borderColor = 'transparent'
                      e.currentTarget.style.boxShadow = '0 4px 20px rgba(0,0,0,0.06)'
                    }}
                  >
                    <div 
                      style={{ 
                        height: '200px', 
                        background: 'linear-gradient(135deg, #FFE5CC, #FFF5EB)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        fontSize: '90px',
                        position: 'relative'
                      }}
                    >
                      {getPetEmoji(pet.species)}
                      <span 
                        style={{ 
                          position: 'absolute', 
                          top: '16px', 
                          left: '16px',
                          background: pet.status === 'adopted' ? '#999' : '#FF8C42',
                          color: '#fff',
                          padding: '6px 16px',
                          borderRadius: '50px',
                          fontSize: '12px',
                          fontWeight: 600
                        }}
                      >
                        {getStatusText(pet.status)}
                      </span>
                    </div>
                    <div style={{ padding: '24px' }}>
                      <div 
                        style={{ 
                          display: 'flex', 
                          justifyContent: 'space-between', 
                          alignItems: 'center', 
                          marginBottom: '8px' 
                        }}
                      >
                        <div 
                          style={{ 
                            fontSize: '20px', 
                            fontWeight: 600, 
                            color: '#4A4A4A' 
                          }}
                        >
                          {pet.name}
                        </div>
                        <div style={{ fontSize: '20px' }}>{getGenderIcon(pet.gender)}</div>
                      </div>
                      <div 
                        style={{ 
                          color: '#FF8C42', 
                          fontSize: '13px', 
                          fontWeight: 500, 
                          marginBottom: '8px' 
                        }}
                      >
                        {pet.breed} · {getAgeText(pet.ageMonths)} · {getSizeText(pet.size)}
                      </div>
                      <div 
                        style={{ 
                          display: 'flex', 
                          gap: '16px', 
                          color: '#999', 
                          fontSize: '13px', 
                          marginBottom: '16px' 
                        }}
                      >
                        🏠 公寓可养 · ⏱ 每日{Math.floor(Math.random() * 3) + 2}h+
                      </div>
                      <div 
                        style={{ 
                          display: 'flex', 
                          gap: '8px', 
                          flexWrap: 'wrap', 
                          marginBottom: '16px' 
                        }}
                      >
                        {pet.personalityTags?.slice(0, 3).map((tag, tagIndex) => (
                          <span
                            key={tagIndex}
                            style={{ 
                              background: 'linear-gradient(135deg, #FFE5CC, #FFD4A8)', 
                              color: '#fff', 
                              padding: '5px 12px', 
                              borderRadius: '50px', 
                              fontSize: '11px' 
                            }}
                          >
                            {tag}
                          </span>
                        ))}
                      </div>
                      <div 
                        style={{ 
                          background: '#F5F5F5', 
                          height: '10px', 
                          borderRadius: '5px', 
                          overflow: 'hidden',
                          marginBottom: '8px'
                        }}
                      >
                        <div 
                          style={{ 
                            height: '100%', 
                            width: `${Math.floor(Math.random() * 30) + 70}%`, 
                            background: 'linear-gradient(90deg, #FF8C42, #FFB347)',
                            borderRadius: '5px'
                          }}
                        />
                      </div>
                      <div style={{ fontSize: '13px', color: '#999' }}>
                        AI 匹配度：<span style={{ color: '#FF8C42', fontWeight: 600 }}>{Math.floor(Math.random() * 30) + 70}%</span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>

              {/* Pagination */}
              {totalPages > 1 && (
                <div 
                  style={{ 
                    display: 'flex', 
                    justifyContent: 'center', 
                    gap: '12px', 
                    marginTop: '60px' 
                  }}
                >
                  <button
                    onClick={() => setCurrentPage(p => Math.max(1, p - 1))}
                    disabled={currentPage === 1}
                    style={{ 
                      width: '48px', 
                      height: '48px', 
                      border: '2px solid #FFE5CC', 
                      borderRadius: '50%', 
                      background: '#fff', 
                      cursor: currentPage === 1 ? 'not-allowed' : 'pointer',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      fontSize: '15px', 
                      color: '#666',
                      opacity: currentPage === 1 ? 0.5 : 1
                    }}
                  >
                    ←
                  </button>
                  {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                    const pageNum = i + 1
                    return (
                      <button
                        key={pageNum}
                        onClick={() => setCurrentPage(pageNum)}
                        style={{ 
                          width: '48px', 
                          height: '48px', 
                          border: currentPage === pageNum ? '2px solid #FF8C42' : '2px solid #FFE5CC', 
                          borderRadius: '50%', 
                          background: currentPage === pageNum ? '#FF8C42' : '#fff', 
                          cursor: 'pointer',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          fontSize: '15px', 
                          color: currentPage === pageNum ? '#fff' : '#666',
                          transition: 'all 0.3s'
                        }}
                      >
                        {pageNum}
                      </button>
                    )
                  })}
                  <button
                    onClick={() => setCurrentPage(p => Math.min(totalPages, p + 1))}
                    disabled={currentPage === totalPages}
                    style={{ 
                      width: '48px', 
                      height: '48px', 
                      border: '2px solid #FFE5CC', 
                      borderRadius: '50%', 
                      background: '#fff', 
                      cursor: currentPage === totalPages ? 'not-allowed' : 'pointer',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      fontSize: '15px', 
                      color: '#666',
                      opacity: currentPage === totalPages ? 0.5 : 1
                    }}
                  >
                    →
                  </button>
                </div>
              )}
            </>
          )}
        </main>
      </div>
    </div>
  )
}
