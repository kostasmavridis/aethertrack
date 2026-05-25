import { api } from './client'
import type { Supplement } from '../types/api'

export const getSupplements = () => api.get<Supplement[]>('/supplements')

// Fallback static list used when the backend is not running yet.
export const STATIC_SUPPLEMENTS: Supplement[] = [
  { id: 1,  name: 'Vitamin D3',          code: 'VIT-D3',   defaultUnit: 'IU' },
  { id: 2,  name: 'Magnesium Glycinate',  code: 'MAG-GLY',  defaultUnit: 'mg' },
  { id: 3,  name: 'Vitamin C',            code: 'VIT-C',    defaultUnit: 'mg' },
  { id: 4,  name: 'Omega-3 Fish Oil',     code: 'OMEGA-3',  defaultUnit: 'mg' },
  { id: 5,  name: 'Zinc Picolinate',      code: 'ZINC-PIC', defaultUnit: 'mg' },
  { id: 6,  name: 'Vitamin B12',          code: 'VIT-B12',  defaultUnit: 'mcg' },
]
