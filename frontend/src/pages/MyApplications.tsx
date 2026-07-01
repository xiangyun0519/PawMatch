import { useState, useEffect } from 'react'
import { message, Spin, Button } from 'antd'
import { applicationApi, AdoptionApplication, PageResult } from '../api/application'
import { petApi, PetProfile } from '../api/pet'
import { useAuthStore } from '../stores/authStore'

export default function MyApplications() {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated)
  const [activeTab, setActiveTab] = useState('all')
  const [applications, setApplications] = useState<AdoptionApplication[]>([])
  const [loading, setLoading] = useState(true)
  const [pageResult, setPageResult] = useState<PageResult<AdoptionApplication> | null>(null)
  const [petCache, setPetCache] = useState<Record<number, PetProfile>>({})

  useEffect(() => {
    if (isAuthenticated) {
      loadApplications()
    } else {
      setLoading(false)
    }
  }, [isAuthenticated, activeTab])

  const loadApplications = async () => {
    setLoading(true)
    
    const response = await applicationApi.getMyApplications(1, 20).catch(() => null)

    setLoading(false)

    if (response?.data) {
      let filtered = response.data.records
      if (activeTab !== 'all') {
        filtered = filtered.filter(app => app.status.toLowerCase() === activeTab)
      }
      setApplications(filtered)
      setPageResult(response.data)
      
      filtered.forEach(app => {
        if (!petCache[app.petId]) {
          loadPetInfo(app.petId)
        }
      })
    }
  }

  const loadPetInfo = async (petId: number) => {
    const response = await petApi.getPet(petId).catch(() => null)
    if (response?.data) {
      setPetCache(prev => ({ ...prev, [petId]: response.data }))
    }
  }

  const tabs = [
    { key: 'all', label: '全部', count: pageResult?.total || 0 },
    { key: 'pending', label: '待审核', count: applications.filter(a => a.status.toLowerCase() === 'pending').length },
    { key: 'approved', label: '已通过', count: applications.filter(a => a.status.toLowerCase() === 'approved').length },
    { key: 'completed', label: '已完成', count: applications.filter(a => a.status.toLowerCase() === 'completed').length },
  ]

  const getPetEmoji = (species: string) => {
    switch (species?.toLowerCase()) {
      case 'dog': return '🐕'
      case 'cat': return '🐱'
      case 'rabbit': return '🐰'
      default: return '🐾'
    }
  }

  const getStatusStyle = (status: string) => {
    switch (status.toLowerCase()) {
      case 'pending':
        return { background: '#FFF3E0', color: '#FF9800' }
      case 'approved':
        return { background: '#E8F5E9', color: '#4CAF50' }
      case 'rejected':
        return { background: '#FFEBEE', color: '#F44336' }
      case 'completed':
        return { background: '#E3F2FD', color: '#2196F3' }
      default:
        return { background: '#F5F5F5', color: '#666' }
    }
  }

  const getStatusText = (status: string) => {
    switch (status.toLowerCase()) {
      case 'pending': return '● 待审核'
      case 'approved': return '✓ 已通过'
      case 'rejected': return '✗ 已拒绝'
      case 'completed': return '✔ 已完成'
      default: return status
    }
  }

  const handleCancel = async (id: number) => {
    const response = await applicationApi.cancel(id).catch(() => null)
    if (response) {
      message.success('已取消申请')
      loadApplications()
    } else {
      message.error('取消失败')
    }
  }

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
        <p style={{ color: '#999', marginBottom: '24px' }}>登录后即可查看你的领养申请</p>
        <Button onClick={() => window.location.href = '/login'} type="primary">
          去登录
        </Button>
      </div>
    )
  }

  return (
    <div style={{ background: '#FFF9F5', minHeight: 'calc(100vh - 80px)', padding: '40px' }}>
      <div style={{ maxWidth: '900px', margin: '0 auto' }}>
        <h1 style={{ fontSize: '32px', fontWeight: 700, color: '#4A4A4A', marginBottom: '8px' }}>我的领养申请</h1>
        <p style={{ color: '#999', fontSize: '15px', marginBottom: '32px' }}>查看和管理你的领养申请进度</p>

        {/* Tabs */}
        <div style={{ display: 'flex', gap: '8px', marginBottom: '32px', background: '#fff', padding: '8px', borderRadius: '50px', width: 'fit-content', boxShadow: '0 2px 12px rgba(0,0,0,0.06)' }}>
          {tabs.map((tab) => (
            <button
              key={tab.key}
              onClick={() => setActiveTab(tab.key)}
              style={{
                padding: '12px 24px',
                borderRadius: '50px',
                fontSize: '14px',
                fontWeight: 500,
                cursor: 'pointer',
                border: 'none',
                background: activeTab === tab.key ? '#FF8C42' : 'transparent',
                color: activeTab === tab.key ? '#fff' : '#666',
                transition: 'all 0.3s'
              }}
            >
              {tab.label} ({tab.count})
            </button>
          ))}
        </div>

        {/* Applications List */}
        {applications.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '80px', background: '#fff', borderRadius: '24px', boxShadow: '0 4px 20px rgba(0,0,0,0.06)' }}>
            <div style={{ fontSize: '80px', marginBottom: '20px' }}>📭</div>
            <div style={{ fontSize: '20px', fontWeight: 600, color: '#4A4A4A', marginBottom: '12px' }}>暂无申请记录</div>
            <div style={{ color: '#999', fontSize: '15px', marginBottom: '24px' }}>快去寻找你的毛孩子吧！</div>
            <button
              onClick={() => window.location.href = '/pets'}
              style={{
                padding: '14px 32px',
                borderRadius: '50px',
                fontSize: '15px',
                fontWeight: 600,
                cursor: 'pointer',
                border: 'none',
                background: 'linear-gradient(135deg, #FF8C42, #FF6B35)',
                color: '#fff',
                boxShadow: '0 4px 16px rgba(255,107,53,0.3)'
              }}
            >
              浏览宠物
            </button>
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
            {applications.map((app) => {
              const pet = petCache[app.petId]
              return (
                <div
                  key={app.id}
                  style={{
                    background: '#fff',
                    borderRadius: '24px',
                    padding: '28px',
                    boxShadow: '0 4px 20px rgba(0,0,0,0.06)',
                    display: 'flex',
                    gap: '24px',
                    cursor: 'pointer',
                    border: '2px solid transparent',
                    transition: 'all 0.3s'
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.borderColor = '#FFE5CC'
                    e.currentTarget.style.transform = 'translateY(-4px)'
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.borderColor = 'transparent'
                    e.currentTarget.style.transform = 'translateY(0)'
                  }}
                >
                  <div
                    style={{
                      width: '120px',
                      height: '120px',
                      borderRadius: '20px',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      fontSize: '64px',
                      flexShrink: 0,
                      background: 'linear-gradient(135deg, #FFE5CC, #FFF5EB)'
                    }}
                  >
                    {pet ? getPetEmoji(pet.species) : '🐾'}
                  </div>

                  <div style={{ flex: 1 }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '8px' }}>
                      <div style={{ fontSize: '22px', fontWeight: 600, color: '#4A4A4A' }}>{pet?.name || `宠物 #${app.petId}`}</div>
                      <span style={{ padding: '6px 14px', borderRadius: '50px', fontSize: '12px', fontWeight: 600, ...getStatusStyle(app.status) }}>
                        {getStatusText(app.status)}
                      </span>
                    </div>

                    <div style={{ color: '#FF8C42', fontSize: '14px', fontWeight: 500, marginBottom: '12px' }}>
                      {pet?.breed || '未知品种'}
                    </div>

                    <div style={{ display: 'flex', gap: '20px', color: '#999', fontSize: '13px', marginBottom: '16px' }}>
                      <span>📅 申请于 {new Date(app.createdAt).toLocaleDateString()}</span>
                    </div>

                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '16px' }}>
                      <div style={{ flex: 1, height: '10px', background: '#F5F5F5', borderRadius: '5px', overflow: 'hidden' }}>
                        <div
                          style={{
                            height: '100%',
                            background: 'linear-gradient(90deg, #FF8C42, #FFB347)',
                            borderRadius: '5px',
                            width: `${app.matchingScore || 85}%`
                          }}
                        />
                      </div>
                      <span style={{ fontSize: '14px', fontWeight: 600, color: '#FF8C42', minWidth: '70px' }}>
                        {app.matchingScore || 85}% 匹配
                      </span>
                    </div>

                    {app.applicantMessage && (
                      <div style={{ background: '#FFF9F5', padding: '16px 20px', borderRadius: '16px', marginBottom: '16px' }}>
                        <div style={{ fontSize: '12px', color: '#999', marginBottom: '6px' }}>我的留言：</div>
                        <div style={{ fontSize: '14px', color: '#666', lineHeight: 1.6 }}>{app.applicantMessage}</div>
                      </div>
                    )}

                    <div style={{ display: 'flex', gap: '12px' }}>
                      <button
                        onClick={() => window.location.href = `/pets/${app.petId}`}
                        style={{
                          padding: '10px 20px',
                          borderRadius: '50px',
                          fontSize: '13px',
                          fontWeight: 500,
                          cursor: 'pointer',
                          border: '2px solid #FFE5CC',
                          background: '#FFF9F5',
                          color: '#FF8C42',
                          transition: 'all 0.3s'
                        }}
                      >
                        查看详情
                      </button>
                      {app.status.toLowerCase() === 'pending' && (
                        <button
                          onClick={() => handleCancel(app.id)}
                          style={{
                            padding: '10px 20px',
                            borderRadius: '50px',
                            fontSize: '13px',
                            fontWeight: 500,
                            cursor: 'pointer',
                            border: 'none',
                            background: '#FFEBEE',
                            color: '#F44336',
                            transition: 'all 0.3s'
                          }}
                        >
                          取消申请
                        </button>
                      )}
                    </div>
                  </div>
                </div>
              )
            })}
          </div>
        )}
      </div>
    </div>
  )
}
