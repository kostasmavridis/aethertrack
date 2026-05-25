import { useCallback, useEffect, useState } from 'react'
import { getRegimenToday } from '../api/regimens'
import type { RegimenTodayResponse } from '../types/api'

export function useRegimenToday(regimenId: number | null) {
  const [today, setToday] = useState<RegimenTodayResponse | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const refresh = useCallback(() => {
    if (!regimenId) return
    setLoading(true)
    setError(null)
    getRegimenToday(regimenId)
      .then(setToday)
      .catch(e => setError(e.message))
      .finally(() => setLoading(false))
  }, [regimenId])

  useEffect(() => {
    refresh()
  }, [refresh])

  return { today, setToday, loading, error, refresh }
}
