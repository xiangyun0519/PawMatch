import api from './request';

export interface ChatRequest {
  sessionId?: number;
  message: string;
}

export interface ChatResponse {
  sessionId: number;
  message: string;
  intent: string;
}

export interface ChatSession {
  id: number;
  userId: number;
  title: string;
  createdAt: string;
  updatedAt: string;
}

export interface ChatMessage {
  id: number;
  sessionId: number;
  role: string;
  content: string;
  metadata: any;
  createdAt: string;
}

export const chatApi = {
  sendMessage: (data: ChatRequest) => 
    api.post<any, { data: ChatResponse }>('/chat', data),
  
  getSessions: () => 
    api.get<any, { data: ChatSession[] }>('/chat/sessions'),
  
  getSessionMessages: (sessionId: number) => 
    api.get<any, { data: ChatMessage[] }>(`/chat/sessions/${sessionId}/messages`),
  
  deleteSession: (sessionId: number) => 
    api.delete<any, { data: null }>(`/chat/sessions/${sessionId}`),
};
