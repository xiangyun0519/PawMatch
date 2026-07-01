import api from './request';

export interface ShelterStats {
  totalPets: number;
  availablePets: number;
  pendingApplications: number;
  completedAdoptions: number;
  monthlyViews: number;
  monthlyNewPets: number;
  monthlyAdoptions: number;
}

export interface PlatformStats {
  totalUsers: number;
  totalAdopters: number;
  totalShelters: number;
  totalPets: number;
  totalApplications: number;
  completedAdoptions: number;
  avgMatchScore: number;
}

export interface MonthlyStats {
  month: string;
  adoptions: number;
  applications: number;
  newPets: number;
}

export interface SpeciesDistribution {
  species: string;
  count: number;
  percentage: number;
}

export interface StatsResponse {
  shelterStats: ShelterStats | null;
  platformStats: PlatformStats;
  monthlyTrend: MonthlyStats[];
  speciesDistribution: SpeciesDistribution[];
}

export const statsApi = {
  getShelterStats: (shelterId: number) => 
    api.get<any, { data: ShelterStats }>(`/stats/shelter/${shelterId}`),
  
  getPlatformStats: () => 
    api.get<any, { data: PlatformStats }>('/stats/platform'),
  
  getMonthlyTrend: (months: number = 6) => 
    api.get<any, { data: MonthlyStats[] }>('/stats/monthly-trend', { params: { months } }),
  
  getSpeciesDistribution: () => 
    api.get<any, { data: SpeciesDistribution[] }>('/stats/species-distribution'),
  
  getFullStats: (shelterId?: number) => 
    api.get<any, { data: StatsResponse }>('/stats/full', { params: { shelterId } }),
};
