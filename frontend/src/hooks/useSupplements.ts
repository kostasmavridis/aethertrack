import { useState, useEffect } from 'react'
import { getSupplements, STATIC_SUPPLEMENTS } from '../api/supplements'
import type { Supplement } from '../types/api'

export function useSupplements() {
  const [supplements, setSupplements] = useState<Supplement[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    getSupplements()
      .then(setSupplements)
      .catch(() => setSupplements(STATIC_SUPPLEMENTS)) // graceful fallback
      .finally(() => setLoading(false))
  }, [])

  return { supplements, loading }
}
