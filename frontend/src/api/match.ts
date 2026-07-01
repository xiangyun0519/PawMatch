import api from './request';

export interface MatchResult {
  petId: number;
  petName: string;
  score: number;
  reasons: string;
  pet: any;
}

export const matchApi = {
  getRecommendations: (topK: number = 5) => 
    api.get<any, { data: MatchResult[] }>('/match/recommend', { params: { topK } }),
  
  matchPet: (petId: number) => 
    api.get<any, { data: MatchResult }>(`/match/pet/${petId}`),
};
