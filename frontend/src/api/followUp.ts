import api from './request';

export interface FollowUpRecord {
  id: number;
  applicationId: number;
  daysAfterAdoption: number;
  photos: string[];
  petHealthStatus: string;
  petBehaviorStatus: string;
  adopterFeedback: string;
  adoptionSatisfaction: number;
  issuesFound: string;
  nextFollowUpDate: string;
  createdAt: string;
}

export interface FollowUpRecordRequest {
  applicationId: number;
  daysAfterAdoption: number;
  photos?: string[];
  petHealthStatus?: string;
  petBehaviorStatus?: string;
  adopterFeedback?: string;
  adoptionSatisfaction?: number;
  issuesFound?: string;
  nextFollowUpDate?: string;
}

export const followUpApi = {
  create: (data: FollowUpRecordRequest) => 
    api.post<any, { data: FollowUpRecord }>('/follow-ups', data),
  
  getByApplication: (applicationId: number) => 
    api.get<any, { data: FollowUpRecord[] }>(`/follow-ups/application/${applicationId}`),
  
  getById: (id: number) => 
    api.get<any, { data: FollowUpRecord }>(`/follow-ups/${id}`),
  
  update: (id: number, data: Partial<FollowUpRecordRequest>) => 
    api.put<any, { data: FollowUpRecord }>(`/follow-ups/${id}`, data),
  
  delete: (id: number) => 
    api.delete<any, { data: null }>(`/follow-ups/${id}`),
  
  getPending: () => 
    api.get<any, { data: FollowUpRecord[] }>('/follow-ups/pending'),
};
