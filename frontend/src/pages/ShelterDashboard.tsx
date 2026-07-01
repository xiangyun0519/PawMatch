import { useState } from 'react'
import { Button, message } from 'antd'
import { Link } from 'react-router-dom'

interface Pet {
  id: number
  emoji: string
  name: string
  breed: string
  age: string
  size: string
  health: string
  status: 'available' | 'pending' | 'adopted'
  statusText: string
  views: number
  bgColor: string
}

interface AdoptionApplication {
  id: number
  petEmoji: string
  petName: string
  applicant: string
  applyDate: string
  matchScore: number
  bgColor: string
}

export default function ShelterDashboard() {
  const [activeTab, setActiveTab] = useState('all')

  const stats = [
    { icon: '🐕', value: '12', label: '在养宠物', change: '↑ 本月新增 3 只' },
    { icon: '📋', value: '8', label: '待审核申请', change: '↑ 5 条新申请' },
    { icon: '✓', value: '45', label: '已成功领养', change: '↑ 本月完成 3 单' },
    { icon: '👁️', value: '1.2k', label: '本月浏览', change: '↑ 15% 增长' },
  ]

  const tabs = [
    { key: 'all', label: '全部宠物' },
    { key: 'available', label: '可领养' },
    { key: 'pending', label: '待审核' },
    { key: 'adopted', label: '已领养' },
  ]

  const pets: Pet[] = [
    {
      id: 1,
      emoji: '🐕',
      name: '小白',
      breed: '柯基',
      age: '3岁',
      size: '中型',
      health: '已绝育、已疫苗',
      status: 'available',
      statusText: '● 可领养',
      views: 328,
      bgColor: 'linear-gradient(135deg, #FFE5CC, #FFF5EB)',
    },
    {
      id: 2,
      emoji: '🐱',
      name: '团子',
      breed: '英短',
      age: '2岁',
      size: '小型',
      health: '已绝育、已疫苗',
      status: 'pending',
      statusText: '● 待审核',
      views: 256,
      bgColor: 'linear-gradient(135deg, #E8D5FF, #D4B8FF)',
    },
    {
      id: 3,
      emoji: '🐕',
      name: '旺财',
      breed: '金毛',
      age: '4岁',
      size: '大型',
      health: '已疫苗',
      status: 'adopted',
      statusText: '✓ 已领养',
      views: 189,
      bgColor: 'linear-gradient(135deg, #D4F0FF, #A8E4FF)',
    },
    {
      id: 4,
      emoji: '🐱',
      name: '花花',
      breed: '狸花',
      age: '1岁',
      size: '中型',
      health: '已绝育、已疫苗',
      status: 'available',
      statusText: '● 可领养',
      views: 412,
      bgColor: 'linear-gradient(135deg, #FFD4D4, #FFB8B8)',
    },
  ]

  const applications: AdoptionApplication[] = [
    {
      id: 1,
      petEmoji: '🐕',
      petName: '祥云 申请领养 小白',
      applicant: '祥云',
      applyDate: '2026-03-20',
      matchScore: 92,
      bgColor: 'linear-gradient(135deg, #FFE5CC, #FFF5EB)',
    },
    {
      id: 2,
      petEmoji: '🐱',
      petName: '张三 申请领养 团子',
      applicant: '张三',
      applyDate: '2026-03-19',
      matchScore: 78,
      bgColor: 'linear-gradient(135deg, #E8D5FF, #D4B8FF)',
    },
    {
      id: 3,
      petEmoji: '🐕',
      petName: '李四 申请领养 旺财',
      applicant: '李四',
      applyDate: '2026-03-18',
      matchScore: 65,
      bgColor: 'linear-gradient(135deg, #D4F0FF, #A8E4FF)',
    },
  ]

  const getStatusClass = (status: string) => {
    switch (status) {
      case 'available':
        return 'bg-[#E8F5E9] text-[#4CAF50]'
      case 'pending':
        return 'bg-[#FFF3E0] text-[#FF9800]'
      case 'adopted':
        return 'bg-[#E3F2FD] text-[#2196F3]'
      default:
        return ''
    }
  }

  const filteredPets =
    activeTab === 'all' ? pets : pets.filter((pet) => pet.status === activeTab)

  const handleAddPet = () => {
    message.info('添加宠物功能开发中...')
  }

  const handleEdit = (_id: number) => {
    message.info('编辑功能开发中...')
  }

  const handleReview = (_id: number) => {
    message.info('审核功能开发中...')
  }

  const handleApprove = (_id: number) => {
    message.success('已通过申请！')
  }

  const handleReject = (_id: number) => {
    message.warning('已拒绝申请')
  }

  return (
    <div className="max-w-7xl mx-auto py-10 px-10">
      <div className="flex gap-4 mb-6">
        <Link to="/follow-ups">
          <Button size="large" className="rounded-full px-6">
            📋 回访记录管理
          </Button>
        </Link>
        <Link to="/stats">
          <Button size="large" className="rounded-full px-6">
            📊 数据统计看板
          </Button>
        </Link>
      </div>
      <div className="grid grid-cols-4 gap-6 mb-10">
        {stats.map((stat, index) => (
          <div key={index} className="bg-white rounded-[24px] p-7 shadow-md">
            <div className="w-14 h-14 bg-gradient-to-br from-[#FFE5CC] to-[#FFD4A8] rounded-4 flex items-center justify-center text-[28px] mb-4">
              {stat.icon}
            </div>
            <div className="text-4xl font-bold text-[#FF8C42] mb-1">{stat.value}</div>
            <div className="text-sm text-[#999]">{stat.label}</div>
            <div className="text-xs text-[#4CAF50] mt-2">{stat.change}</div>
          </div>
        ))}
      </div>

      <div className="flex justify-between items-center mb-8">
        <h1 className="text-3xl font-bold text-[#4A4A4A]">🐾 宠物管理</h1>
        <Button
          type="primary"
          size="large"
          className="rounded-full px-7 flex items-center gap-2"
          style={{ background: 'linear-gradient(135deg, #FF8C42, #FF6B35)' }}
          onClick={handleAddPet}
        >
          ➕ 添加宠物
        </Button>
      </div>

      <div className="flex gap-2 mb-7 bg-white px-2 py-2 rounded-full shadow-md w-fit">
        {tabs.map((tab) => (
          <button
            key={tab.key}
            onClick={() => setActiveTab(tab.key)}
            className={`px-7 py-3 rounded-full text-sm font-medium cursor-pointer transition-all border-none ${
              activeTab === tab.key
                ? 'bg-[#FF8C42] text-white'
                : 'text-[#666] hover:text-[#FF8C42]'
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      <div className="bg-white rounded-[24px] shadow-md overflow-hidden mb-10">
        <table className="w-full border-collapse">
          <thead>
            <tr>
              <th className="text-left px-6 py-5 bg-[#FFF9F5] text-xs font-semibold text-[#999] border-b-2 border-[#FFE5CC]">
                宠物信息
              </th>
              <th className="text-left px-6 py-5 bg-[#FFF9F5] text-xs font-semibold text-[#999] border-b-2 border-[#FFE5CC]">
                年龄/体型
              </th>
              <th className="text-left px-6 py-5 bg-[#FFF9F5] text-xs font-semibold text-[#999] border-b-2 border-[#FFE5CC]">
                健康状况
              </th>
              <th className="text-left px-6 py-5 bg-[#FFF9F5] text-xs font-semibold text-[#999] border-b-2 border-[#FFE5CC]">
                状态
              </th>
              <th className="text-left px-6 py-5 bg-[#FFF9F5] text-xs font-semibold text-[#999] border-b-2 border-[#FFE5CC]">
                浏览量
              </th>
              <th className="text-left px-6 py-5 bg-[#FFF9F5] text-xs font-semibold text-[#999] border-b-2 border-[#FFE5CC]">
                操作
              </th>
            </tr>
          </thead>
          <tbody>
            {filteredPets.map((pet) => (
              <tr key={pet.id} className="hover:bg-[#FFFDF9]">
                <td className="px-6 py-5 border-b-2 border-[#FFF9F5]">
                  <div className="flex items-center gap-3.5">
                    <div
                      className="w-14 h-14 rounded-3.5 flex items-center justify-center text-[28px]"
                      style={{ background: pet.bgColor }}
                    >
                      {pet.emoji}
                    </div>
                    <div>
                      <div className="text-[15px] font-semibold text-[#4A4A4A] mb-1">{pet.name}</div>
                      <div className="text-xs text-[#999]">{pet.breed}</div>
                    </div>
                  </div>
                </td>
                <td className="px-6 py-5 border-b-2 border-[#FFF9F5] text-sm text-[#4A4A4A]">
                  {pet.age} / {pet.size}
                </td>
                <td className="px-6 py-5 border-b-2 border-[#FFF9F5] text-sm text-[#4A4A4A]">
                  {pet.health}
                </td>
                <td className="px-6 py-5 border-b-2 border-[#FFF9F5]">
                  <span className={`px-3.5 py-1.5 rounded-full text-xs font-semibold ${getStatusClass(pet.status)}`}>
                    {pet.statusText}
                  </span>
                </td>
                <td className="px-6 py-5 border-b-2 border-[#FFF9F5] text-sm text-[#4A4A4A]">
                  {pet.views}
                </td>
                <td className="px-6 py-5 border-b-2 border-[#FFF9F5]">
                  <div className="flex gap-2">
                    {pet.status === 'pending' ? (
                      <button
                        onClick={() => handleReview(pet.id)}
                        className="px-4 py-2 rounded-full text-xs font-medium cursor-pointer transition-all border-none bg-[#FF8C42] text-white hover:bg-[#FF6B35]"
                      >
                        审核
                      </button>
                    ) : (
                      <button
                        onClick={() => handleEdit(pet.id)}
                        className="px-4 py-2 rounded-full text-xs font-medium cursor-pointer transition-all border-none bg-[#FFF9F5] text-[#FF8C42] border-2 border-[#FFE5CC] hover:bg-[#FFE5CC]"
                      >
                        编辑
                      </button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <h2 className="text-xl font-semibold text-[#4A4A4A] mb-5">📋 领养申请待审核</h2>

      <div className="flex flex-col gap-4">
        {applications.map((app) => (
          <div
            key={app.id}
            className="bg-white rounded-[24px] p-6 shadow-md flex gap-5 items-center"
          >
            <div
              className="w-16 h-16 rounded-4 flex items-center justify-center text-[32px]"
              style={{ background: app.bgColor }}
            >
              {app.petEmoji}
            </div>
            <div className="flex-1">
              <div className="text-base font-semibold text-[#4A4A4A] mb-1">{app.petName}</div>
              <div className="text-sm text-[#999]">申请时间：{app.applyDate} · AI匹配度：{app.matchScore}%</div>
            </div>
            <div className="text-lg font-bold text-[#FF8C42] mr-6">{app.matchScore}%</div>
            <div className="flex gap-2">
              <button
                onClick={() => handleApprove(app.id)}
                className="px-5 py-2.5 bg-[#4CAF50] text-white border-none rounded-full text-sm font-semibold cursor-pointer hover:opacity-90"
              >
                ✓ 通过
              </button>
              <button
                onClick={() => handleReject(app.id)}
                className="px-5 py-2.5 bg-white text-[#F44336] border-2 border-[#FFCDD2] rounded-full text-sm font-semibold cursor-pointer hover:bg-[#FFEBEE]"
              >
                ✕ 拒绝
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
