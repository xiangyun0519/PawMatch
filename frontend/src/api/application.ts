import api from './request';

export interface AdoptionApplication {
  id: number;
  adopterId: number;
  petId: number;
  status: string;
  matchingScore: number;
  matchingReasons: string;
  applicantMessage: string;
  shelterReviewNote: string;
  reviewedAt: string;
  completedAt: string;
  createdAt: string;
  updatedAt: string;
}

export interface ApplicationCreateRequest {
  petId: number;
  applicantMessage?: string;
}

export interface ApplicationReviewRequest {
  status: string;
  shelterReviewNote?: string;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
}

export const applicationApi = {
  create: (data: ApplicationCreateRequest) => 
    api.post<any, { data: AdoptionApplication }>('/applications', data),
  
  getMyApplications: (page: number = 1, pageSize: number = 10) => 
    api.get<any, { data: PageResult<AdoptionApplication> }>('/applications/my', { params: { page, pageSize } }),
  
  getShelterApplications: (shelterId: number, page: number = 1, pageSize: number = 10) => 
    api.get<any, { data: PageResult<AdoptionApplication> }>('/applications/shelter', { params: { shelterId, page, pageSize } }),
  
  getById: (id: number) => 
    api.get<any, { data: AdoptionApplication }>(`/applications/${id}`),
  
  review: (id: number, data: ApplicationReviewRequest) => 
    api.post<any, { data: AdoptionApplication }>(`/applications/${id}/review`, data),
  
  complete: (id: number) => 
    api.post<any, { data: AdoptionApplication }>(`/applications/${id}/complete`),
  
  cancel: (id: number) => 
    api.post<any, { data: null }>(`/applications/${id}/cancel`),
};
