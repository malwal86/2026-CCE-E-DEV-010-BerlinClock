import { useCallback, useRef } from 'react'

/**
 * Starting a request returns a check that stays true until the next request starts, so a slow answer never replaces
 * what was asked for after it.
 */
export function useLatestRequest(): () => () => boolean {
  const latest = useRef(0)
  return useCallback(() => {
    const request = ++latest.current
    return () => request === latest.current
  }, [])
}
