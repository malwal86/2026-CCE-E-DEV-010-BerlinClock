import type { Conversion } from './types'

// Resolved against the page origin so the same code works behind the Vite proxy, nginx and in tests.
const url = (path: string) => new URL(path, window.location.origin)

/** An RFC 9457 problem detail, as the API returns for every error. */
interface ProblemDetail {
  title: string
  status: number
  detail: string
}

/** The API refused the request; `message` is its detail, written to be shown to the user. */
export class ApiProblem extends Error {
  readonly status: number

  constructor(problem: ProblemDetail) {
    super(problem.detail)
    this.name = 'ApiProblem'
    this.status = problem.status
  }
}

export async function convertTime(time: string): Promise<Conversion> {
  const response = await fetch(url('/api/conversions'), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ time }),
  })
  if (!response.ok) {
    throw new ApiProblem(await response.json())
  }
  return response.json()
}

export async function fetchRecentConversions(): Promise<Conversion[]> {
  const response = await fetch(url('/api/conversions'))
  return response.json()
}
