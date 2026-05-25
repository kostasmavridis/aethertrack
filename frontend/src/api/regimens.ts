import { api } from './client'
import type { CreateRegimenRequest, RegimenResponse } from '../types/api'

export const createRegimen = (req: CreateRegimenRequest) =>
  api.post<RegimenResponse>('/regimens', req)

export const listRegimens = () => api.get<RegimenResponse[]>('/regimens')
