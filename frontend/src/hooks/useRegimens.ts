import { useState, useEffect, useCallback } from 'react'
import { listRegimens } from '../api/regimens'
import type { RegimenResponse } from '../types/api'

export function useRegimens() {
  const [regimens, setRegimens] = useState<RegimenResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError]     = useState<string | null>(null)

  const refresh = useCallback(() => {
    setLoading(true)
    listRegimens()
      .then(setRegimens)
      .catch(e => setError(e.message))
      .finally(() => setLoading(false))
  }, [])

  useEffect(refresh, [refresh])

  return { regimens, loading, error, refresh }
}
