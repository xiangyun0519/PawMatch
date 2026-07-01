import api from './request';

export interface PetProfile {
  id: number;
  name: string;
  species: string;
  breed: string;
  ageMonths: number;
  gender: string;
  size: string;
  healthStatus: string;
  personalityTags: string[];
  description: string;
  photos: string[];
  shelterId: number;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface PetQueryRequest {
  species?: string;
  gender?: string;
  size?: string;
  minAge?: number;
  maxAge?: number;
  healthStatus?: string;
  personalityTags?: string[];
  shelterId?: number;
  status?: string;
  page?: number;
  pageSize?: number;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
}

export const petApi = {
  queryPets: (params: PetQueryRequest) => 
    api.get<any, { data: PageResult<PetProfile> }>('/pets', { params }),
  
  getPet: (id: number) => 
    api.get<any, { data: PetProfile }>(`/pets/${id}`),
  
  createPet: (data: Partial<PetProfile>) => 
    api.post<any, { data: PetProfile }>('/pets', data),
  
  updatePet: (id: number, data: Partial<PetProfile>) => 
    api.put<any, { data: PetProfile }>(`/pets/${id}`, data),
  
  deletePet: (id: number) => 
    api.delete<any, { data: null }>(`/pets/${id}`),
  
  getRecommended: (limit: number = 8) => 
    api.get<any, { data: PetProfile[] }>('/pets/recommended', { params: { limit } }),
  
  getByShelter: (shelterId: number) => 
    api.get<any, { data: PetProfile[] }>(`/pets/shelter/${shelterId}`),
};
