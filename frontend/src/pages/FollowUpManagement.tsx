import { useState, useEffect } from 'react'
import { Button, message, Spin, Empty, Tag, Progress, Modal } from 'antd'
import { applicationApi, AdoptionApplication } from '../api/application'
import { statsApi, ShelterStats } from '../api/stats'
import FollowUpModal, { FollowUpList } from '../components/FollowUpModal'
import dayjs from 'dayjs'

export default function FollowUpManagement() {
  const [applications, setApplications] = useState<AdoptionApplication[]>([])
  const [loading, setLoading] = useState(true)
  const [stats, setStats] = useState<ShelterStats | null>(null)
  const [selectedApplication, setSelectedApplication] = useState<AdoptionApplication | null>(null)
  const [modalOpen, setModalOpen] = useState(false)
  const [detailModalOpen, setDetailModalOpen] = useState(false)
  const [shelterId] = useState(1)

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    setLoading(true)
    try {
      const [appRes, statsRes] = await Promise.all([
        applicationApi.getShelterApplications(shelterId, 1, 50),
        statsApi.getShelterStats(shelterId),
      ])
      const completedApps = appRes.data.records.filter(app => app.status === 'COMPLETED')
      setApplications(completedApps)
      setStats(statsRes.data)
    } catch {
      message.error('加载数据失败')
    } finally {
      setLoading(false)
    }
  }

  const handleViewDetail = (app: AdoptionApplication) => {
    setSelectedApplication(app)
    setDetailModalOpen(true)
  }

  const handleCreateFollowUp = (app: AdoptionApplication) => {
    setSelectedApplication(app)
    setModalOpen(true)
  }

  const handleFollowUpSuccess = () => {
    loadData()
  }

  const getStatusTag = (status: string) => {
    const statusMap: Record<string, { color: string; text: string }> = {
      PENDING: { color: 'orange', text: '待审核' },
      APPROVED: { color: 'blue', text: '已通过' },
      REJECTED: { color: 'red', text: '已拒绝' },
      COMPLETED: { color: 'green', text: '已完成' },
      CANCELLED: { color: 'default', text: '已取消' },
    }
    const config = statusMap[status] || { color: 'default', text: status }
    return <Tag color={config.color}>{config.text}</Tag>
  }

  const getMatchScoreColor = (score: number) => {
    if (score >= 80) return '#4CAF50'
    if (score >= 60) return '#FF9800'
    return '#F44336'
  }

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-[400px]">
        <Spin size="large" />
      </div>
    )
  }

  return (
    <div className="max-w-7xl mx-auto py-10 px-10">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-[#4A4A4A] mb-2">📋 回访记录管理</h1>
        <p className="text-[#999]">管理已完成领养的回访记录，跟踪宠物领养后的生活状况</p>
      </div>

      {stats && (
        <div className="grid grid-cols-4 gap-6 mb-10">
          <div className="bg-white rounded-[24px] p-7 shadow-md">
            <div className="w-14 h-14 bg-gradient-to-br from-[#E8F5E9] to-[#C8E6C9] rounded-4 flex items-center justify-center text-[28px] mb-4">
              ✓
            </div>
            <div className="text-4xl font-bold text-[#4CAF50] mb-1">{stats.completedAdoptions}</div>
            <div className="text-sm text-[#999]">已完成领养</div>
          </div>
          <div className="bg-white rounded-[24px] p-7 shadow-md">
            <div className="w-14 h-14 bg-gradient-to-br from-[#E3F2FD] to-[#BBDEFB] rounded-4 flex items-center justify-center text-[28px] mb-4">
              📝
            </div>
            <div className="text-4xl font-bold text-[#2196F3] mb-1">{applications.length}</div>
            <div className="text-sm text-[#999]">待回访申请</div>
          </div>
          <div className="bg-white rounded-[24px] p-7 shadow-md">
            <div className="w-14 h-14 bg-gradient-to-br from-[#FFF3E0] to-[#FFE0B2] rounded-4 flex items-center justify-center text-[28px] mb-4">
              🐕
            </div>
            <div className="text-4xl font-bold text-[#FF8C42] mb-1">{stats.totalPets}</div>
            <div className="text-sm text-[#999]">在养宠物</div>
          </div>
          <div className="bg-white rounded-[24px] p-7 shadow-md">
            <div className="w-14 h-14 bg-gradient-to-br from-[#F3E5F5] to-[#E1BEE7] rounded-4 flex items-center justify-center text-[28px] mb-4">
              ⭐
            </div>
            <div className="text-4xl font-bold text-[#9C27B0] mb-1">{stats.monthlyAdoptions}</div>
            <div className="text-sm text-[#999]">本月领养</div>
          </div>
        </div>
      )}

      {applications.length === 0 ? (
        <Empty description="暂无已完成的领养申请" className="py-20" />
      ) : (
        <div className="space-y-4">
          {applications.map((app) => (
            <div
              key={app.id}
              className="bg-white rounded-[24px] p-6 shadow-md hover:shadow-lg transition-shadow"
            >
              <div className="flex items-center justify-between">
                <div className="flex-1">
                  <div className="flex items-center gap-3 mb-2">
                    <span className="text-lg font-semibold text-[#4A4A4A]">
                      申请 #{app.id}
                    </span>
                    {getStatusTag(app.status)}
                    {app.matchingScore && (
                      <div className="flex items-center gap-2">
                        <Progress
                          percent={Number(app.matchingScore)}
                          size="small"
                          style={{ width: 100 }}
                          strokeColor={getMatchScoreColor(Number(app.matchingScore))}
                        />
                        <span className="text-sm text-[#999]">匹配度</span>
                      </div>
                    )}
                  </div>
                  <div className="text-sm text-[#999]">
                    完成时间：{app.completedAt ? dayjs(app.completedAt).format('YYYY-MM-DD HH:mm') : '-'}
                  </div>
                  {app.matchingReasons && (
                    <div className="text-sm text-[#666] mt-2 bg-[#FFF9F5] p-3 rounded-xl">
                      💡 {app.matchingReasons}
                    </div>
                  )}
                </div>
                <div className="flex gap-3">
                  <Button
                    type="default"
                    onClick={() => handleViewDetail(app)}
                    className="rounded-full"
                  >
                    查看详情
                  </Button>
                  <Button
                    type="primary"
                    onClick={() => handleCreateFollowUp(app)}
                    className="rounded-full"
                    style={{ background: 'linear-gradient(135deg, #FF8C42, #FF6B35)' }}
                  >
                    新建回访
                  </Button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      <FollowUpModal
        open={modalOpen}
        applicationId={selectedApplication?.id || null}
        onClose={() => setModalOpen(false)}
        onSuccess={handleFollowUpSuccess}
      />

      <Modal
        title={`申请 #${selectedApplication?.id} 回访记录`}
        open={detailModalOpen}
        onCancel={() => setDetailModalOpen(false)}
        footer={null}
        width={700}
      >
        {selectedApplication && (
          <div className="py-4">
            <div className="mb-6 p-4 bg-[#FFF9F5] rounded-xl">
              <div className="text-sm text-[#999] mb-2">申请信息</div>
              <div className="flex gap-4">
                <div>
                  <span className="text-[#666]">宠物ID：</span>
                  <span className="font-semibold text-[#4A4A4A]">{selectedApplication.petId}</span>
                </div>
                <div>
                  <span className="text-[#666]">领养人ID：</span>
                  <span className="font-semibold text-[#4A4A4A]">{selectedApplication.adopterId}</span>
                </div>
                <div>
                  <span className="text-[#666]">完成时间：</span>
                  <span className="font-semibold text-[#4A4A4A]">
                    {selectedApplication.completedAt 
                      ? dayjs(selectedApplication.completedAt).format('YYYY-MM-DD') 
                      : '-'}
                  </span>
                </div>
              </div>
            </div>
            <FollowUpList applicationId={selectedApplication.id} />
          </div>
        )}
      </Modal>
    </div>
  )
}
