import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';


export interface RatingSummaryDTO {
  avgScore: number;
  totalCount: number;
  scoreDistribution: { [key: string]: number };
  topTags: string[];
}

export interface RatingRequestDTO {
  tripId: string;
  rateeId: string;
  score: number;
  comment: string;
  tags: string[];
}

@Injectable({ providedIn: 'root' })
export class RatingService {
  private readonly BASE_URL = '/api/v1/ratings';

  constructor(private http: HttpClient) {}

  getRatingSummary(rateeId: string): Observable<any> {
  return this.http.get(`http://localhost:8010/api/v1/ratings/user/${rateeId}`);
}

}
