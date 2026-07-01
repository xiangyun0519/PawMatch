import { useState, useRef, useEffect } from 'react'
import { Spin, message as antdMessage } from 'antd'
import { chatApi, ChatSession, ChatMessage } from '../api/chat'
import { useAuthStore } from '../stores/authStore'

interface UIMessage {
  id: number
  type: 'ai' | 'user'
  content: string
}

const WELCOME: UIMessage = {
  id: 0,
  type: 'ai',
  content: `你好！我是 PawMatch 的 AI 助手 👋

我可以帮助你：
• 找到最适合你的宠物
• 解答领养相关问题
• 提供养宠知识和建议

有什么我可以帮你的吗？`,
}

export default function Chat() {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated)
  const [messages, setMessages] = useState<UIMessage[]>([WELCOME])
  const [inputValue, setInputValue] = useState('')
  const [loading, setLoading] = useState(false)
  const [currentSessionId, setCurrentSessionId] = useState<number | undefined>()
  const [sessions, setSessions] = useState<ChatSession[]>([])
  const [historyLoading, setHistoryLoading] = useState(false)
  const messagesEndRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (isAuthenticated) {
      loadSessions()
    }
  }, [isAuthenticated])

  const loadSessions = async () => {
    const response = await chatApi.getSessions().catch(() => null)
    if (response?.data) {
      setSessions(response.data)
    }
  }

  const openSession = async (sessionId: number) => {
    if (currentSessionId === sessionId) return
    setHistoryLoading(true)
    setCurrentSessionId(sessionId)
    const response = await chatApi.getSessionMessages(sessionId).catch(() => null)
    setHistoryLoading(false)
    if (response?.data) {
      const ui: UIMessage[] = response.data.map((m: ChatMessage) => ({
        id: m.id,
        type: m.role === 'user' ? 'user' : 'ai',
        content: m.content,
      }))
      setMessages(ui.length > 0 ? ui : [WELCOME])
    }
  }

  const handleDeleteSession = async (sessionId: number) => {
    await chatApi.deleteSession(sessionId).catch(() => null)
    if (currentSessionId === sessionId) handleNewChat()
    loadSessions()
  }

  const quickQuestions = [
    '🐱 推荐适合新手的宠物',
    '🏠 公寓适合养什么狗？',
    '💰 养宠物每月花费多少？',
    '📋 领养流程是什么？',
    '🏥 如何判断宠物健康？',
  ]

  const aiFeatures = [
    { icon: '🎯', title: '智能匹配推荐' },
    { icon: '📚', title: '领养知识问答' },
    { icon: '🏥', title: '宠物护理咨询' },
    { icon: '💡', title: '性格分析匹配' },
  ]

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }

  useEffect(() => {
    scrollToBottom()
  }, [messages, loading])

  const handleSend = async () => {
    if (!inputValue.trim() || loading) return

    const userText = inputValue
    const tempId = Date.now()
    setMessages((prev) => [...prev, { id: tempId, type: 'user', content: userText }])
    setInputValue('')
    setLoading(true)

    const response = await chatApi.sendMessage({
      sessionId: currentSessionId,
      message: userText,
    }).catch(() => null)

    setLoading(false)

    if (response?.data) {
      const sid = response.data.sessionId
      if (!currentSessionId && sid) {
        setCurrentSessionId(sid)
        loadSessions() // 新会话出现，刷新侧边栏
      }
      setMessages((prev) => [
        ...prev,
        { id: tempId + 1, type: 'ai', content: response.data.message },
      ])
    } else {
      antdMessage.error('AI 助手暂时无法响应')
      setMessages((prev) => [
        ...prev,
        {
          id: tempId + 1,
          type: 'ai',
          content: '抱歉，我暂时无法连接到服务器。请稍后再试。',
        },
      ])
    }
  }

  const handleQuickQuestion = (question: string) => {
    const cleaned = question.replace(/^[^一-龥A-Za-z]+/, '').trim()
    setInputValue(cleaned)
  }

  const handleNewChat = () => {
    setMessages([WELCOME])
    setCurrentSessionId(undefined)
  }

  return (
    <div style={{ background: '#FFF9F5', minHeight: 'calc(100vh - 80px)', padding: '32px' }}>
      <div
        style={{
          display: 'flex',
          maxWidth: '1100px',
          margin: '0 auto',
          width: '100%',
          gap: '28px',
          height: 'calc(100vh - 144px)',
        }}
      >
        {/* Chat Area */}
        <div
          style={{
            flex: 1,
            background: '#fff',
            borderRadius: '28px',
            boxShadow: '0 6px 32px rgba(0,0,0,0.08)',
            display: 'flex',
            flexDirection: 'column',
            overflow: 'hidden',
          }}
        >
          <div
            style={{
              padding: '24px 28px',
              borderBottom: '2px solid #FFF9F5',
              display: 'flex',
              alignItems: 'center',
              gap: '16px',
              justifyContent: 'space-between',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
              <div
                style={{
                  width: '56px',
                  height: '56px',
                  background: 'linear-gradient(135deg, #FF8C42, #FF6B35)',
                  borderRadius: '16px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: '28px',
                }}
              >
                🤖
              </div>
              <div>
                <div style={{ fontSize: '18px', fontWeight: 600, color: '#4A4A4A' }}>AI 领养助手</div>
                <div style={{ fontSize: '13px', color: '#4CAF50' }}>● 在线 · 混合检索增强</div>
              </div>
            </div>
            <button
              onClick={handleNewChat}
              style={{
                padding: '8px 16px',
                borderRadius: '50px',
                fontSize: '13px',
                fontWeight: 500,
                cursor: 'pointer',
                border: '2px solid #FFE5CC',
                background: '#fff',
                color: '#FF8C42',
                transition: 'all 0.3s',
              }}
            >
              🔄 新对话
            </button>
          </div>

          <div style={{ flex: 1, padding: '28px', overflowY: 'auto' }}>
            {historyLoading && (
              <div style={{ textAlign: 'center', padding: '24px' }}>
                <Spin />
              </div>
            )}
            {messages.map((message, idx) => (
              <div
                key={`${message.id}-${idx}`}
                style={{
                  display: 'flex',
                  gap: '14px',
                  marginBottom: '24px',
                  flexDirection: message.type === 'user' ? 'row-reverse' : 'row',
                }}
              >
                <div
                  style={{
                    width: '42px',
                    height: '42px',
                    borderRadius: '50%',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontSize: '18px',
                    flexShrink: 0,
                    background:
                      message.type === 'ai'
                        ? 'linear-gradient(135deg, #FF8C42, #FFB347)'
                        : '#FFE5CC',
                    color: message.type === 'ai' ? '#fff' : '#4A4A4A',
                  }}
                >
                  {message.type === 'ai' ? '🤖' : '👤'}
                </div>
                <div style={{ maxWidth: '72%' }}>
                  <div
                    style={{
                      padding: '18px 22px',
                      borderRadius: '20px',
                      lineHeight: 1.7,
                      fontSize: '15px',
                      background:
                        message.type === 'ai'
                          ? '#FFF9F5'
                          : 'linear-gradient(135deg, #FF8C42, #FF6B35)',
                      color: message.type === 'ai' ? '#4A4A4A' : '#fff',
                      borderTopLeftRadius: message.type === 'ai' ? '6px' : '20px',
                      borderTopRightRadius: message.type === 'user' ? '6px' : '20px',
                      whiteSpace: 'pre-wrap',
                    }}
                  >
                    {message.content}
                  </div>
                </div>
              </div>
            ))}
            {loading && (
              <div style={{ display: 'flex', gap: '14px', marginBottom: '24px' }}>
                <div
                  style={{
                    width: '42px',
                    height: '42px',
                    borderRadius: '50%',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontSize: '18px',
                    background: 'linear-gradient(135deg, #FF8C42, #FFB347)',
                    color: '#fff',
                  }}
                >
                  🤖
                </div>
                <div style={{ padding: '18px 22px', borderRadius: '20px', background: '#FFF9F5' }}>
                  <Spin size="small" />
                </div>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          <div
            style={{
              padding: '24px 28px',
              borderTop: '2px solid #FFF9F5',
              display: 'flex',
              gap: '16px',
              alignItems: 'center',
            }}
          >
            <input
              type="text"
              value={inputValue}
              onChange={(e) => setInputValue(e.targetValue)}
              onKeyPress={(e) => e.key === 'Enter' && handleSend()}
              placeholder="输入你的问题..."
              disabled={loading}
              style={{
                flex: 1,
                padding: '16px 24px',
                border: '2px solid #FFE5CC',
                borderRadius: '50px',
                fontSize: '15px',
                outline: 'none',
                transition: 'border-color 0.3s',
                fontFamily: 'inherit',
                background: loading ? '#F5F5F5' : '#fff',
              }}
            />
            <button
              onClick={handleSend}
              disabled={loading || !inputValue.trim()}
              style={{
                width: '54px',
                height: '54px',
                background:
                  loading || !inputValue.trim()
                    ? '#FFE5CC'
                    : 'linear-gradient(135deg, #FF8C42, #FF6B35)',
                border: 'none',
                borderRadius: '50%',
                color: '#fff',
                fontSize: '22px',
                cursor: loading || !inputValue.trim() ? 'not-allowed' : 'pointer',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                transition: 'transform 0.3s',
                boxShadow:
                  loading || !inputValue.trim()
                    ? 'none'
                    : '0 4px 16px rgba(255,107,53,0.3)',
                opacity: loading || !inputValue.trim() ? 0.6 : 1,
              }}
            >
              ➤
            </button>
          </div>
        </div>

        {/* Sidebar */}
        <aside style={{ width: '280px', flexShrink: 0, display: 'flex', flexDirection: 'column', gap: '20px' }}>
          <div
            style={{
              background: '#fff',
              borderRadius: '24px',
              padding: '24px',
              boxShadow: '0 4px 20px rgba(0,0,0,0.06)',
            }}
          >
            <div style={{ fontSize: '15px', fontWeight: 600, color: '#4A4A4A', marginBottom: '16px' }}>
              历史对话
            </div>
            {!isAuthenticated && (
              <div style={{ fontSize: '13px', color: '#999' }}>登录后查看</div>
            )}
            {isAuthenticated && sessions.length === 0 && (
              <div style={{ fontSize: '13px', color: '#999' }}>暂无历史对话</div>
            )}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', maxHeight: '240px', overflowY: 'auto' }}>
              {sessions.map((s) => (
                <div
                  key={s.id}
                  onClick={() => openSession(s.id)}
                  style={{
                    padding: '10px 14px',
                    borderRadius: '12px',
                    background: currentSessionId === s.id ? '#FFE5CC' : '#FFF9F5',
                    cursor: 'pointer',
                    fontSize: '13px',
                    color: '#4A4A4A',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                  }}
                >
                  <span
                    style={{
                      overflow: 'hidden',
                      textOverflow: 'ellipsis',
                      whiteSpace: 'nowrap',
                      flex: 1,
                    }}
                  >
                    {s.title || `对话 ${s.id}`}
                  </span>
                  <span
                    onClick={(e) => {
                      e.stopPropagation()
                      handleDeleteSession(s.id)
                    }}
                    style={{ color: '#FF8C42', marginLeft: '8px', cursor: 'pointer' }}
                    title="删除"
                  >
                    ×
                  </span>
                </div>
              ))}
            </div>
          </div>

          <div
            style={{
              background: '#fff',
              borderRadius: '24px',
              padding: '24px',
              boxShadow: '0 4px 20px rgba(0,0,0,0.06)',
            }}
          >
            <div style={{ fontSize: '15px', fontWeight: 600, color: '#4A4A4A', marginBottom: '16px' }}>
              快捷问题
            </div>
            {quickQuestions.map((question, index) => (
              <div
                key={index}
                onClick={() => handleQuickQuestion(question)}
                style={{
                  display: 'block',
                  padding: '14px 18px',
                  background: '#FFF9F5',
                  borderRadius: '14px',
                  marginBottom: '10px',
                  color: '#666',
                  textDecoration: 'none',
                  fontSize: '14px',
                  transition: 'all 0.3s',
                  cursor: 'pointer',
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.background = '#FFE5CC'
                  e.currentTarget.style.color = '#FF8C42'
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.background = '#FFF9F5'
                  e.currentTarget.style.color = '#666'
                }}
              >
                {question}
              </div>
            ))}
          </div>

          <div
            style={{
              background: '#fff',
              borderRadius: '24px',
              padding: '24px',
              boxShadow: '0 4px 20px rgba(0,0,0,0.06)',
            }}
          >
            <div style={{ fontSize: '15px', fontWeight: 600, color: '#4A4A4A', marginBottom: '16px' }}>
              AI 助手能力
            </div>
            {aiFeatures.map((feature, index) => (
              <div
                key={index}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '12px',
                  marginBottom: '14px',
                  fontSize: '14px',
                  color: '#666',
                }}
              >
                <div
                  style={{
                    width: '34px',
                    height: '34px',
                    background: 'linear-gradient(135deg, #FFE5CC, #FFD4A8)',
                    borderRadius: '10px',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontSize: '16px',
                  }}
                >
                  {feature.icon}
                </div>
                <span>{feature.title}</span>
              </div>
            ))}
          </div>
        </aside>
      </div>
    </div>
  )
}