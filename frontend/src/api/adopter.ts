import api from './request';

export interface AdopterProfile {
  id: number;
  userId: number;
  housingType: string;
  hasChildren: boolean;
  hasElderly: boolean;
  hasOtherPets: boolean;
  petExperience: string;
  dailyHoursAvailable: number;
  preferredPetSize: string[];
  preferredPetAge: string[];
  allergyInfo: string;
  activityLevel: string;
  adoptionMotivation: string;
  createdAt: string;
  updatedAt: string;
}

export interface AdopterProfileRequest {
  housingType: string;
  hasChildren: boolean;
  hasElderly: boolean;
  hasOtherPets: boolean;
  petExperience: string;
  dailyHoursAvailable: number;
  preferredPetSize: string[];
  preferredPetAge: string[];
  allergyInfo: string;
  activityLevel: string;
  adoptionMotivation: string;
}

export const adopterApi = {
  getProfile: () => 
    api.get<any, { data: AdopterProfile }>('/adopters/profile'),
  
  updateProfile: (data: AdopterProfileRequest) => 
    api.put<any, { data: AdopterProfile }>('/adopters/profile', data),
  
  getById: (id: number) => 
    api.get<any, { data: AdopterProfile }>(`/adopters/${id}`),
};
