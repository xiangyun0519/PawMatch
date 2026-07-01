import { Outlet, Link, useLocation, useNavigate } from 'react-router-dom'
import { Layout as AntLayout, Dropdown } from 'antd'
import { useAuthStore } from '../stores/authStore'

const { Header, Content, Footer } = AntLayout

export default function Layout() {
  const location = useLocation()
  const navigate = useNavigate()
  const { user, isAuthenticated, logout } = useAuthStore()

  const navItems = [
    { path: '/', label: '首页' },
    { path: '/pets', label: '宠物列表' },
    { path: '/chat', label: 'AI匹配助手' },
    { path: '/profile', label: '我的画像' },
    { path: '/applications', label: '我的申请' },
    { path: '/stats', label: '数据统计' },
    { path: '/about', label: '关于我们' },
  ]

  return (
    <AntLayout style={{ minHeight: '100vh', background: '#FFF9F5' }}>
      <Header 
        style={{ 
          display: 'flex', 
          alignItems: 'center', 
          justifyContent: 'space-between', 
          padding: '0 60px',
          background: '#fff',
          boxShadow: '0 4px 20px rgba(0,0,0,0.06)',
          position: 'sticky',
          top: 0,
          zIndex: 100,
          height: '80px'
        }}
      >
        <Link to="/" style={{ display: 'flex', alignItems: 'center', gap: '12px', textDecoration: 'none' }}>
          <div 
            style={{ 
              width: '48px', 
              height: '48px', 
              background: 'linear-gradient(135deg, #FF8C42, #FFB347)', 
              borderRadius: '14px', 
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: '#fff', 
              fontSize: '24px',
              boxShadow: '0 4px 12px rgba(255,140,66,0.2)'
            }}
          >
            🐾
          </div>
          <span 
            style={{ 
              fontSize: '28px', 
              fontWeight: 700, 
              color: '#FF8C42',
              fontFamily: '"Noto Sans SC", sans-serif'
            }}
          >
            PawMatch
          </span>
        </Link>

        <nav style={{ display: 'flex', gap: '48px' }}>
          {navItems.map((item) => (
            <Link
              key={item.path}
              to={item.path}
              style={{ 
                fontSize: '16px', 
                fontWeight: 500, 
                textDecoration: 'none',
                padding: '8px 0',
                position: 'relative',
                color: location.pathname === item.path || (item.path === '/pets' && location.pathname.startsWith('/pets')) ? '#FF8C42' : '#666',
                transition: 'color 0.3s'
              }}
            >
              {item.label}
              {(location.pathname === item.path || (item.path === '/pets' && location.pathname.startsWith('/pets'))) && (
                <span 
                  style={{ 
                    position: 'absolute', 
                    bottom: '-8px', 
                    left: 0, 
                    width: '100%', 
                    height: '4px', 
                    background: 'linear-gradient(90deg, #FF8C42, #FFB347)', 
                    borderRadius: '2px' 
                  }} 
                />
              )}
            </Link>
          ))}
        </nav>

        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          {isAuthenticated ? (
            <Dropdown
              menu={{
                items: [
                  {
                    key: 'profile',
                    label: <Link to="/profile" style={{ textDecoration: 'none' }}>我的画像</Link>,
                  },
                  {
                    key: 'applications',
                    label: <Link to="/applications" style={{ textDecoration: 'none' }}>我的申请</Link>,
                  },
                  {
                    key: 'logout',
                    label: '退出登录',
                    onClick: () => {
                      logout()
                      navigate('/')
                    },
                  },
                ],
              }}
              placement="bottomRight"
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }}>
                <span style={{ color: '#999', fontSize: '14px' }}>欢迎，</span>
                <span style={{ fontSize: '16px', fontWeight: 600, color: '#FF8C42' }}>
                  {user?.username || '用户'}
                </span>
                {user?.avatarUrl ? (
                  <img
                    src={user.avatarUrl}
                    alt="avatar"
                    style={{
                      width: '32px',
                      height: '32px',
                      borderRadius: '50%',
                      objectFit: 'cover',
                    }}
                  />
                ) : (
                  <div
                    style={{
                      width: '32px',
                      height: '32px',
                      borderRadius: '50%',
                      background: 'linear-gradient(135deg, #FF8C42, #FFB347)',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      color: '#fff',
                      fontSize: '14px',
                    }}
                  >
                    {user?.username?.charAt(0)?.toUpperCase() || '👤'}
                  </div>
                )}
              </div>
            </Dropdown>
          ) : (
            <>
              <span style={{ color: '#999', fontSize: '14px' }}>欢迎，</span>
              <Link to="/login" style={{ fontSize: '16px', fontWeight: 600, color: '#FF8C42', textDecoration: 'none' }}>登录</Link>
              <span style={{ color: '#999', fontSize: '14px' }}>/</span>
              <Link to="/register" style={{ fontSize: '16px', fontWeight: 600, color: '#FF8C42', textDecoration: 'none' }}>注册</Link>
            </>
          )}
        </div>
      </Header>

      <Content style={{ flex: 1, background: '#FFF9F5' }}>
        <Outlet />
      </Content>

      <Footer 
        style={{ 
          textAlign: 'center', 
          background: '#fff', 
          borderTop: '2px solid #FFE5CC',
          padding: '80px 40px'
        }}
      >
        <div 
          style={{ 
            display: 'flex', 
            justifyContent: 'center', 
            gap: '40px', 
            marginBottom: '20px' 
          }}
        >
          <a 
            href="#" 
            style={{ 
              color: '#666', 
              textDecoration: 'none', 
              fontSize: '14px',
              transition: 'color 0.3s'
            }}
          >
            关于我们
          </a>
          <a 
            href="#" 
            style={{ 
              color: '#666', 
              textDecoration: 'none', 
              fontSize: '14px',
              transition: 'color 0.3s'
            }}
          >
            帮助中心
          </a>
          <a 
            href="#" 
            style={{ 
              color: '#666', 
              textDecoration: 'none', 
              fontSize: '14px',
              transition: 'color 0.3s'
            }}
          >
            隐私政策
          </a>
          <a 
            href="#" 
            style={{ 
              color: '#666', 
              textDecoration: 'none', 
              fontSize: '14px',
              transition: 'color 0.3s'
            }}
          >
            联系客服
          </a>
        </div>
        <p style={{ color: '#999', fontSize: '14px' }}>
          © 2026 PawMatch · 让<span style={{ color: '#FF6B35', fontWeight: 600 }}>爱</span>找到家 🐾
        </p>
      </Footer>
    </AntLayout>
  )
}
