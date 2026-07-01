import { Link } from 'react-router-dom'

export default function AboutUs() {
  const stats = [
    { number: '2,800+', label: '成功匹配', sub: '累计帮助宠物找到新家' },
    { number: '120+', label: '合作救助站', sub: '覆盖全国主要城市' },
    { number: '96%', label: '匹配满意度', sub: '用户反馈好评率' },
    { number: '50,000+', label: '注册用户', sub: '爱心领养社区成员' },
  ]

  const missions = [
    { icon: '🎯', title: '智能匹配', desc: '通过 AI 算法分析用户偏好与宠物性格，实现精准一对一匹配，提高领养成功率。' },
    { icon: '📚', title: '科普教育', desc: '提供科学的养宠知识，帮助领养人做好充分准备，构建负责任的养宠文化。' },
    { icon: '🤝', title: '连接善意', desc: '搭建救助站与领养人之间的桥梁，让每一份善意都能找到出口。' },
  ]

  const values = [
    { emoji: '❤️', title: '善意为先', desc: '每一次匹配都以动物的福祉为第一优先级' },
    { emoji: '🔬', title: '科学决策', desc: '用数据说话，让匹配有据可依' },
    { emoji: '🌈', title: '多元包容', desc: '尊重每一种养宠方式，欢迎每一个爱动物的你' },
    { emoji: '🔒', title: '隐私安全', desc: '严格保护用户信息，安全可信的平台' },
  ]

  const miniNav = [
    { label: '👥 团队介绍', href: '#' },
    { label: '💡 项目愿景', href: '#' },
    { label: '🤝 合作伙伴', href: '#' },
    { label: '📖 使用指南', href: '#' },
    { label: '📞 联系我们', href: '#' },
  ]

  return (
    <div style={{ background: '#FFF9F5', minHeight: 'calc(100vh - 80px)' }}>
      {/* Hero Section */}
      <section 
        style={{ 
          background: 'linear-gradient(180deg, #FFE5CC, #FFF5EB)', 
          padding: '80px 60px', 
          textAlign: 'center', 
          position: 'relative', 
          overflow: 'hidden' 
        }}
      >
        <div 
          style={{ 
            position: 'absolute', 
            top: '-40px', 
            right: '-40px', 
            width: '200px', 
            height: '200px', 
            background: 'radial-gradient(circle, rgba(255,140,66,0.15) 0%, transparent 70%)', 
            borderRadius: '50%' 
          }} 
        />
        <div 
          style={{ 
            position: 'absolute', 
            bottom: '-30px', 
            left: '-30px', 
            width: '150px', 
            height: '150px', 
            background: 'radial-gradient(circle, rgba(255,179,71,0.1) 0%, transparent 70%)', 
            borderRadius: '50%' 
          }} 
        />
        <div style={{ position: 'relative', zIndex: 1 }}>
          <span 
            style={{ 
              display: 'inline-block', 
              background: '#fff', 
              padding: '8px 20px', 
              borderRadius: '50px', 
              fontSize: '13px', 
              color: '#FF8C42', 
              marginBottom: '20px', 
              boxShadow: '0 4px 16px rgba(0,0,0,0.08)' 
            }}
          >
            🐾 PawMatch
          </span>
          <h1 style={{ fontSize: '56px', fontWeight: 700, color: '#4A4A4A', marginBottom: '16px' }}>关于我们</h1>
          <p style={{ fontSize: '18px', color: 'rgba(74,74,74,0.8)', maxWidth: '700px', margin: '0 auto', lineHeight: 1.8 }}>
            用科技传递温暖，让每一只流浪动物都能找到那个最适合的家。<br />在这里，爱与缘分从未如此简单。
          </p>
        </div>
      </section>

      {/* Content */}
      <div style={{ maxWidth: '1200px', margin: '0 auto', padding: '80px 40px' }}>
        {/* Story Section */}
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '60px', alignItems: 'center', marginBottom: '100px' }}>
          <div>
            <h2 style={{ fontSize: '40px', fontWeight: 700, color: '#4A4A4A', marginBottom: '20px' }}>
              我们的<span style={{ color: '#FF8C42' }}>故事</span>
            </h2>
            <p style={{ fontSize: '16px', color: '#666', lineHeight: 2, marginBottom: '16px' }}>
              PawMatch 诞生于 2025 年底，最初只是一个校园公益项目。我们注意到，身边许多同学想要领养宠物，却不知道从何下手；而各大救助站的毛孩子们，却常常等不到一个合适的领养人。
            </p>
            <p style={{ fontSize: '16px', color: '#666', lineHeight: 2, marginBottom: '16px' }}>
              于是，我们决定用技术解决这个问题 —— 用 AI 算法连接流浪动物与爱心人士，让领养变得<strong>更科学、更简单、更有温度</strong>。
            </p>
            <div 
              style={{ 
                background: 'linear-gradient(135deg, #FFF5EB, #FFE5CC)', 
                padding: '24px 28px', 
                borderRadius: '16px', 
                borderLeft: '4px solid #FF8C42', 
                marginTop: '24px' 
              }}
            >
              <p style={{ color: '#FF8C42', fontWeight: 600, fontSize: '15px', margin: 0 }}>
                💡 "每一只流浪的小生命，都值得被温柔以待。我们希望用代码，为它们找到回家的路。"
              </p>
            </div>
          </div>
          <div 
            style={{ 
              background: 'linear-gradient(135deg, #FFE5CC, #FFD4A8)', 
              borderRadius: '32px', 
              height: '400px', 
              display: 'flex', 
              alignItems: 'center', 
              justifyContent: 'center', 
              position: 'relative', 
              overflow: 'hidden' 
            }}
          >
            <div 
              style={{ 
                position: 'absolute', 
                top: '20px', 
                right: '20px', 
                width: '120px', 
                height: '120px', 
                background: 'rgba(255,255,255,0.3)', 
                borderRadius: '50%' 
              }} 
            />
            <span style={{ fontSize: '180px', opacity: 0.9 }}>🏠</span>
          </div>
        </div>

        {/* Stats Section */}
        <div 
          style={{ 
            background: '#fff', 
            borderRadius: '28px', 
            padding: '60px 80px', 
            boxShadow: '0 6px 24px rgba(0,0,0,0.06)', 
            marginBottom: '100px' 
          }}
        >
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '40px', textAlign: 'center' }}>
            {stats.map((stat, index) => (
              <div key={index} style={{ padding: '20px' }}>
                <div 
                  style={{ 
                    fontSize: '56px', 
                    fontWeight: 700, 
                    background: 'linear-gradient(135deg, #FF8C42, #FF6B35)', 
                    WebkitBackgroundClip: 'text', 
                    WebkitTextFillColor: 'transparent', 
                    marginBottom: '8px' 
                  }}
                >
                  {stat.number}
                </div>
                <div style={{ fontSize: '15px', color: '#999', fontWeight: 500 }}>{stat.label}</div>
                <div style={{ fontSize: '12px', color: '#ccc', marginTop: '4px' }}>{stat.sub}</div>
              </div>
            ))}
          </div>
        </div>

        {/* Mission Section */}
        <div style={{ marginBottom: '100px' }}>
          <div style={{ textAlign: 'center', marginBottom: '50px' }}>
            <h2 style={{ fontSize: '42px', fontWeight: 700, color: '#4A4A4A', marginBottom: '12px' }}>我们的使命</h2>
            <p style={{ fontSize: '16px', color: '#999' }}>用 AI 技术赋能流浪动物领养，让爱与缘分精准相遇</p>
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '32px' }}>
            {missions.map((mission, index) => (
              <div 
                key={index} 
                style={{ 
                  background: '#fff', 
                  borderRadius: '24px', 
                  padding: '40px', 
                  textAlign: 'center', 
                  boxShadow: '0 6px 24px rgba(0,0,0,0.06)', 
                  border: '2px solid transparent', 
                  transition: 'all 0.3s',
                  cursor: 'pointer'
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.borderColor = '#FFB347'
                  e.currentTarget.style.transform = 'translateY(-4px)'
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.borderColor = 'transparent'
                  e.currentTarget.style.transform = 'translateY(0)'
                }}
              >
                <div 
                  style={{ 
                    width: '80px', 
                    height: '80px', 
                    background: 'linear-gradient(135deg, #FFE5CC, #FFD4A8)', 
                    borderRadius: '20px', 
                    display: 'flex', 
                    alignItems: 'center', 
                    justifyContent: 'center', 
                    fontSize: '40px', 
                    margin: '0 auto 20px' 
                  }}
                >
                  {mission.icon}
                </div>
                <h3 style={{ fontSize: '20px', color: '#4A4A4A', marginBottom: '12px' }}>{mission.title}</h3>
                <p style={{ fontSize: '14px', color: '#888', lineHeight: 1.8 }}>{mission.desc}</p>
              </div>
            ))}
          </div>
        </div>

        {/* Values Section */}
        <div 
          style={{ 
            background: '#fff', 
            borderRadius: '28px', 
            padding: '80px', 
            boxShadow: '0 6px 24px rgba(0,0,0,0.06)', 
            marginBottom: '100px' 
          }}
        >
          <div style={{ textAlign: 'center', marginBottom: '50px' }}>
            <h2 style={{ fontSize: '42px', fontWeight: 700, color: '#4A4A4A', marginBottom: '12px' }}>核心价值观</h2>
            <p style={{ fontSize: '16px', color: '#999' }}>这些信念驱动着我们前行</p>
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '32px' }}>
            {values.map((value, index) => (
              <div 
                key={index} 
                style={{ 
                  textAlign: 'center', 
                  padding: '32px 20px', 
                  background: '#FFF9F5', 
                  borderRadius: '20px', 
                  transition: 'all 0.3s',
                  cursor: 'pointer'
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.background = '#FFE5CC'
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.background = '#FFF9F5'
                }}
              >
                <div style={{ fontSize: '48px', marginBottom: '16px' }}>{value.emoji}</div>
                <h4 style={{ fontSize: '18px', color: '#4A4A4A', marginBottom: '8px' }}>{value.title}</h4>
                <p style={{ fontSize: '13px', color: '#999', lineHeight: 1.7 }}>{value.desc}</p>
              </div>
            ))}
          </div>
        </div>

        {/* Mini Navigation */}
        <div style={{ display: 'flex', justifyContent: 'center', gap: '16px', marginBottom: '60px', flexWrap: 'wrap' }}>
          {miniNav.map((item, index) => (
            <a 
              key={index} 
              href={item.href}
              style={{ 
                padding: '12px 28px', 
                background: '#fff', 
                borderRadius: '50px', 
                textDecoration: 'none', 
                color: '#666', 
                fontSize: '14px', 
                fontWeight: 500, 
                boxShadow: '0 4px 16px rgba(0,0,0,0.06)', 
                transition: 'all 0.3s', 
                border: '2px solid transparent' 
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.background = '#FF8C42'
                e.currentTarget.style.color = '#fff'
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.background = '#fff'
                e.currentTarget.style.color = '#666'
              }}
            >
              {item.label}
            </a>
          ))}
        </div>

        {/* CTA Section */}
        <div 
          style={{ 
            background: 'linear-gradient(135deg, #FF8C42, #FF6B35)', 
            borderRadius: '28px', 
            padding: '80px 60px', 
            textAlign: 'center', 
            marginBottom: '60px', 
            position: 'relative', 
            overflow: 'hidden' 
          }}
        >
          <div 
            style={{ 
              position: 'absolute', 
              top: '-50px', 
              right: '-50px', 
              width: '200px', 
              height: '200px', 
              background: 'rgba(255,255,255,0.1)', 
              borderRadius: '50%' 
            }} 
          />
          <div style={{ position: 'relative', zIndex: 1 }}>
            <h2 style={{ fontSize: '40px', color: '#fff', marginBottom: '16px', fontWeight: 700 }}>加入我们的爱心社区</h2>
            <p style={{ fontSize: '16px', color: 'rgba(255,255,255,0.9)', marginBottom: '32px' }}>
              无论你是想领养宠物，还是想为流浪动物出一份力<br />PawMatch 都欢迎你的到来
            </p>
            <Link 
              to="/pets"
              style={{ 
                background: '#fff', 
                color: '#FF8C42', 
                padding: '16px 40px', 
                borderRadius: '50px', 
                textDecoration: 'none', 
                fontWeight: 600, 
                fontSize: '16px', 
                display: 'inline-block', 
                boxShadow: '0 8px 24px rgba(0,0,0,0.15)', 
                transition: 'all 0.3s' 
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.transform = 'translateY(-3px)'
                e.currentTarget.style.boxShadow = '0 12px 32px rgba(0,0,0,0.2)'
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.transform = 'translateY(0)'
                e.currentTarget.style.boxShadow = '0 8px 24px rgba(0,0,0,0.15)'
              }}
            >
              开始探索 🐾
            </Link>
          </div>
        </div>
      </div>
    </div>
  )
}
