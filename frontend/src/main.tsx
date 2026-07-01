import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { ConfigProvider, theme } from 'antd'
import zhCN from 'antd/locale/zh_CN'
import App from './App'
import './index.css'

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <BrowserRouter>
      <ConfigProvider
        locale={zhCN}
        theme={{
          algorithm: theme.defaultAlgorithm,
          token: {
            colorPrimary: '#FF8C42',
            colorBgBase: '#FFF9F5',
            borderRadius: 12,
          },
          components: {
            Button: {
              borderRadius: 20,
            },
            Card: {
              borderRadiusLG: 20,
            },
            Input: {
              borderRadius: 12,
            },
            Select: {
              borderRadius: 12,
            },
          },
        }}
      >
        <App />
      </ConfigProvider>
    </BrowserRouter>
  </React.StrictMode>,
)
