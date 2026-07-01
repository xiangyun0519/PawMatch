import { useState, useEffect } from 'react'
import { Spin, Progress } from 'antd'
import { statsApi, StatsResponse, MonthlyStats, SpeciesDistribution } from '../api/stats'

export default function StatsDashboard() {
  const [loading, setLoading] = useState(true)
  const [stats, setStats] = useState<StatsResponse | null>(null)
  const [shelterId] = useState(1)

  useEffect(() => {
    loadStats()
  }, [])

  const loadStats = async () => {
    setLoading(true)
    try {
      const res = await statsApi.getFullStats(shelterId)
      setStats(res.data)
    } catch {
      console.error('加载统计数据失败')
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-[400px]">
        <Spin size="large" />
      </div>
    )
  }

  if (!stats) {
    return <div className="text-center py-20 text-[#999]">暂无统计数据</div>
  }

  const { platformStats, shelterStats, monthlyTrend, speciesDistribution } = stats

  const getSpeciesEmoji = (species: string) => {
    const emojiMap: Record<string, string> = {
      DOG: '🐕',
      CAT: '🐱',
      BIRD: '🐦',
      RABBIT: '🐰',
      OTHER: '🐾',
    }
    return emojiMap[species] || '🐾'
  }

  const getSpeciesName = (species: string) => {
    const nameMap: Record<string, string> = {
      DOG: '狗狗',
      CAT: '猫咪',
      BIRD: '鸟类',
      RABBIT: '兔子',
      OTHER: '其他',
    }
    return nameMap[species] || species
  }

  return (
    <div className="max-w-7xl mx-auto py-10 px-10">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-[#4A4A4A] mb-2">📊 数据统计看板</h1>
        <p className="text-[#999]">平台运营数据概览，实时掌握领养动态</p>
      </div>

      {shelterStats && (
        <div className="mb-10">
          <h2 className="text-xl font-semibold text-[#4A4A4A] mb-5">🏠 救助站数据</h2>
          <div className="grid grid-cols-4 gap-6">
            <div className="bg-white rounded-[24px] p-7 shadow-md">
              <div className="w-14 h-14 bg-gradient-to-br from-[#FFE5CC] to-[#FFD4A8] rounded-4 flex items-center justify-center text-[28px] mb-4">
                🐕
              </div>
              <div className="text-4xl font-bold text-[#FF8C42] mb-1">{shelterStats.totalPets}</div>
              <div className="text-sm text-[#999]">在养宠物</div>
              <div className="text-xs text-[#4CAF50] mt-2">↑ 本月新增 {shelterStats.monthlyNewPets} 只</div>
            </div>
            <div className="bg-white rounded-[24px] p-7 shadow-md">
              <div className="w-14 h-14 bg-gradient-to-br from-[#E8F5E9] to-[#C8E6C9] rounded-4 flex items-center justify-center text-[28px] mb-4">
                ✓
              </div>
              <div className="text-4xl font-bold text-[#4CAF50] mb-1">{shelterStats.availablePets}</div>
              <div className="text-sm text-[#999]">可领养</div>
            </div>
            <div className="bg-white rounded-[24px] p-7 shadow-md">
              <div className="w-14 h-14 bg-gradient-to-br from-[#FFF3E0] to-[#FFE0B2] rounded-4 flex items-center justify-center text-[28px] mb-4">
                📋
              </div>
              <div className="text-4xl font-bold text-[#FF9800] mb-1">{shelterStats.pendingApplications}</div>
              <div className="text-sm text-[#999]">待审核申请</div>
            </div>
            <div className="bg-white rounded-[24px] p-7 shadow-md">
              <div className="w-14 h-14 bg-gradient-to-br from-[#E3F2FD] to-[#BBDEFB] rounded-4 flex items-center justify-center text-[28px] mb-4">
                💕
              </div>
              <div className="text-4xl font-bold text-[#2196F3] mb-1">{shelterStats.completedAdoptions}</div>
              <div className="text-sm text-[#999]">已成功领养</div>
              <div className="text-xs text-[#4CAF50] mt-2">↑ 本月完成 {shelterStats.monthlyAdoptions} 单</div>
            </div>
          </div>
        </div>
      )}

      <div className="mb-10">
        <h2 className="text-xl font-semibold text-[#4A4A4A] mb-5">🌐 平台数据</h2>
        <div className="grid grid-cols-4 gap-6">
          <div className="bg-white rounded-[24px] p-7 shadow-md">
            <div className="w-14 h-14 bg-gradient-to-br from-[#F3E5F5] to-[#E1BEE7] rounded-4 flex items-center justify-center text-[28px] mb-4">
              👥
            </div>
            <div className="text-4xl font-bold text-[#9C27B0] mb-1">{platformStats.totalUsers}</div>
            <div className="text-sm text-[#999]">注册用户</div>
          </div>
          <div className="bg-white rounded-[24px] p-7 shadow-md">
            <div className="w-14 h-14 bg-gradient-to-br from-[#FFEBEE] to-[#FFCDD2] rounded-4 flex items-center justify-center text-[28px] mb-4">
              💗
            </div>
            <div className="text-4xl font-bold text-[#E91E63] mb-1">{platformStats.totalAdopters}</div>
            <div className="text-sm text-[#999]">领养人</div>
          </div>
          <div className="bg-white rounded-[24px] p-7 shadow-md">
            <div className="w-14 h-14 bg-gradient-to-br from-[#E0F2F1] to-[#B2DFDB] rounded-4 flex items-center justify-center text-[28px] mb-4">
              🏠
            </div>
            <div className="text-4xl font-bold text-[#009688] mb-1">{platformStats.totalShelters}</div>
            <div className="text-sm text-[#999]">救助站</div>
          </div>
          <div className="bg-white rounded-[24px] p-7 shadow-md">
            <div className="w-14 h-14 bg-gradient-to-br from-[#FFF8E1] to-[#FFECB3] rounded-4 flex items-center justify-center text-[28px] mb-4">
              ⭐
            </div>
            <div className="text-4xl font-bold text-[#FFC107] mb-1">{platformStats.avgMatchScore.toFixed(1)}</div>
            <div className="text-sm text-[#999]">平均匹配分</div>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-2 gap-8">
        <div>
          <h2 className="text-xl font-semibold text-[#4A4A4A] mb-5">📈 近6个月趋势</h2>
          <div className="bg-white rounded-[24px] p-7 shadow-md">
            <div className="space-y-4">
              {monthlyTrend.map((item: MonthlyStats) => (
                <div key={item.month} className="flex items-center gap-4">
                  <div className="w-20 text-sm text-[#666]">{item.month}</div>
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-1">
                      <span className="text-xs text-[#999]">领养</span>
                      <div className="flex-1 bg-[#FFE5CC] rounded-full h-3">
                        <div
                          className="bg-[#FF8C42] h-3 rounded-full"
                          style={{ width: `${Math.min(item.adoptions * 10, 100)}%` }}
                        />
                      </div>
                      <span className="text-sm font-semibold text-[#FF8C42] w-8">{item.adoptions}</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <span className="text-xs text-[#999]">申请</span>
                      <div className="flex-1 bg-[#E3F2FD] rounded-full h-3">
                        <div
                          className="bg-[#2196F3] h-3 rounded-full"
                          style={{ width: `${Math.min(item.applications * 5, 100)}%` }}
                        />
                      </div>
                      <span className="text-sm font-semibold text-[#2196F3] w-8">{item.applications}</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        <div>
          <h2 className="text-xl font-semibold text-[#4A4A4A] mb-5">🐾 宠物种类分布</h2>
          <div className="bg-white rounded-[24px] p-7 shadow-md">
            {speciesDistribution.length === 0 ? (
              <div className="text-center py-8 text-[#999]">暂无数据</div>
            ) : (
              <div className="space-y-4">
                {speciesDistribution.map((item: SpeciesDistribution) => (
                  <div key={item.species} className="flex items-center gap-4">
                    <div className="w-12 h-12 bg-gradient-to-br from-[#FFE5CC] to-[#FFD4A8] rounded-4 flex items-center justify-center text-2xl">
                      {getSpeciesEmoji(item.species)}
                    </div>
                    <div className="flex-1">
                      <div className="flex justify-between mb-1">
                        <span className="font-semibold text-[#4A4A4A]">{getSpeciesName(item.species)}</span>
                        <span className="text-sm text-[#999]">{item.count} 只</span>
                      </div>
                      <Progress
                        percent={item.percentage}
                        size="small"
                        strokeColor="#FF8C42"
                        showInfo={false}
                      />
                    </div>
                    <div className="text-sm font-semibold text-[#FF8C42] w-14 text-right">
                      {item.percentage}%
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>

      <div className="mt-10">
        <h2 className="text-xl font-semibold text-[#4A4A4A] mb-5">📊 平台总览</h2>
        <div className="bg-white rounded-[24px] p-7 shadow-md">
          <div className="grid grid-cols-3 gap-8">
            <div className="text-center p-6 bg-[#FFF9F5] rounded-2xl">
              <div className="text-5xl font-bold text-[#FF8C42] mb-2">{platformStats.totalPets}</div>
              <div className="text-[#666]">总宠物数</div>
            </div>
            <div className="text-center p-6 bg-[#E8F5E9] rounded-2xl">
              <div className="text-5xl font-bold text-[#4CAF50] mb-2">{platformStats.completedAdoptions}</div>
              <div className="text-[#666]">成功领养</div>
            </div>
            <div className="text-center p-6 bg-[#E3F2FD] rounded-2xl">
              <div className="text-5xl font-bold text-[#2196F3] mb-2">{platformStats.totalApplications}</div>
              <div className="text-[#666]">总申请数</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
