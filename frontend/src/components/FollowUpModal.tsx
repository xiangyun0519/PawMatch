import { useState, useEffect } from 'react'
import { Modal, Form, Input, DatePicker, Rate, message, Spin, Empty } from 'antd'
import { followUpApi, FollowUpRecord, FollowUpRecordRequest } from '../api/followUp'
import { applicationApi } from '../api/application'
import dayjs from 'dayjs'

interface FollowUpModalProps {
  open: boolean
  applicationId: number | null
  onClose: () => void
  onSuccess: () => void
}

export default function FollowUpModal({ open, applicationId, onClose, onSuccess }: FollowUpModalProps) {
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (open && applicationId) {
      loadApplication()
    }
  }, [open, applicationId])

  const loadApplication = async () => {
    if (!applicationId) return
    setLoading(true)
    try {
      const res = await applicationApi.getById(applicationId)
      const completedDays = res.data.completedAt 
        ? Math.ceil((new Date().getTime() - new Date(res.data.completedAt).getTime()) / (1000 * 60 * 60 * 24))
        : 0
      form.setFieldsValue({ daysAfterAdoption: completedDays })
    } catch {
      message.error('加载申请信息失败')
    } finally {
      setLoading(false)
    }
  }

  const handleSubmit = async () => {
    if (!applicationId) return
    const values = await form.validateFields()
    setLoading(true)
    try {
      const data: FollowUpRecordRequest = {
        applicationId,
        daysAfterAdoption: values.daysAfterAdoption,
        petHealthStatus: values.petHealthStatus,
        petBehaviorStatus: values.petBehaviorStatus,
        adopterFeedback: values.adopterFeedback,
        adoptionSatisfaction: values.adoptionSatisfaction,
        issuesFound: values.issuesFound,
        nextFollowUpDate: values.nextFollowUpDate?.format('YYYY-MM-DD'),
      }
      await followUpApi.create(data)
      message.success('回访记录创建成功')
      form.resetFields()
      onSuccess()
      onClose()
    } catch {
      message.error('创建回访记录失败')
    } finally {
      setLoading(false)
    }
  }

  return (
    <Modal
      title="📝 新建回访记录"
      open={open}
      onCancel={onClose}
      onOk={handleSubmit}
      confirmLoading={loading}
      width={600}
      okText="提交"
      cancelText="取消"
    >
      <Spin spinning={loading}>
        <Form form={form} layout="vertical" className="mt-4">
          <Form.Item
            name="daysAfterAdoption"
            label="领养后天数"
            rules={[{ required: true, message: '请输入领养后天数' }]}
          >
            <Input type="number" placeholder="请输入领养后天数" suffix="天" />
          </Form.Item>
          
          <Form.Item name="petHealthStatus" label="宠物健康状况">
            <Input.TextArea rows={2} placeholder="请描述宠物当前健康状况" />
          </Form.Item>
          
          <Form.Item name="petBehaviorStatus" label="宠物行为状态">
            <Input.TextArea rows={2} placeholder="请描述宠物当前行为状态" />
          </Form.Item>
          
          <Form.Item name="adopterFeedback" label="领养人反馈">
            <Input.TextArea rows={2} placeholder="请输入领养人反馈" />
          </Form.Item>
          
          <Form.Item name="adoptionSatisfaction" label="领养满意度">
            <Rate />
          </Form.Item>
          
          <Form.Item name="issuesFound" label="发现问题">
            <Input.TextArea rows={2} placeholder="请描述发现的问题（如有）" />
          </Form.Item>
          
          <Form.Item name="nextFollowUpDate" label="下次回访日期">
            <DatePicker className="w-full" />
          </Form.Item>
        </Form>
      </Spin>
    </Modal>
  )
}

export function FollowUpList({ applicationId }: { applicationId: number }) {
  const [records, setRecords] = useState<FollowUpRecord[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadRecords()
  }, [applicationId])

  const loadRecords = async () => {
    setLoading(true)
    try {
      const res = await followUpApi.getByApplication(applicationId)
      setRecords(res.data)
    } catch {
      message.error('加载回访记录失败')
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return <div className="flex justify-center py-8"><Spin /></div>
  }

  if (records.length === 0) {
    return <Empty description="暂无回访记录" />
  }

  return (
    <div className="space-y-4">
      {records.map((record) => (
        <div key={record.id} className="bg-white rounded-2xl p-5 shadow-md">
          <div className="flex justify-between items-start mb-3">
            <div className="text-sm text-[#FF8C42] font-semibold">
              领养后第 {record.daysAfterAdoption} 天
            </div>
            <div className="text-xs text-[#999]">
              {dayjs(record.createdAt).format('YYYY-MM-DD HH:mm')}
            </div>
          </div>
          
          {record.petHealthStatus && (
            <div className="mb-2">
              <span className="text-xs text-[#999]">健康状况：</span>
              <span className="text-sm text-[#4A4A4A]">{record.petHealthStatus}</span>
            </div>
          )}
          
          {record.petBehaviorStatus && (
            <div className="mb-2">
              <span className="text-xs text-[#999]">行为状态：</span>
              <span className="text-sm text-[#4A4A4A]">{record.petBehaviorStatus}</span>
            </div>
          )}
          
          {record.adopterFeedback && (
            <div className="mb-2">
              <span className="text-xs text-[#999]">领养人反馈：</span>
              <span className="text-sm text-[#4A4A4A]">{record.adopterFeedback}</span>
            </div>
          )}
          
          {record.adoptionSatisfaction && (
            <div className="mb-2">
              <span className="text-xs text-[#999]">满意度：</span>
              <Rate disabled defaultValue={record.adoptionSatisfaction} className="text-sm" />
            </div>
          )}
          
          {record.issuesFound && (
            <div className="mb-2">
              <span className="text-xs text-[#999]">发现问题：</span>
              <span className="text-sm text-[#F44336]">{record.issuesFound}</span>
            </div>
          )}
          
          {record.nextFollowUpDate && (
            <div className="mt-3 pt-3 border-t border-[#FFE5CC]">
              <span className="text-xs text-[#999]">下次回访：</span>
              <span className="text-sm text-[#FF8C42]">{record.nextFollowUpDate}</span>
            </div>
          )}
        </div>
      ))}
    </div>
  )
}
