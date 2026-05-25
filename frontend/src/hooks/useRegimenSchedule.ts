import { useCallback, useEffect, useState } from 'react'
import { getRegimenSchedule } from '../api/regimens'
import type { RegimenScheduleResponse } from '../types/api'

export function useRegimenSchedule(regimenId: number | null) {
  const [schedule, setSchedule] = useState<RegimenScheduleResponse | null>(null)
  const [loading, setLoading]   = useState(false)
  const [error, setError]       = useState<string | null>(null)

  const refresh = useCallback(() => {
    if (!regimenId) return
    setLoading(true)
    setError(null)
    getRegimenSchedule(regimenId)
      .then(setSchedule)
      .catch(e => setError(e.message))
      .finally(() => setLoading(false))
  }, [regimenId])

  useEffect(() => {
    refresh()
  }, [refresh])

  return { schedule, loading, error, refresh }
}
