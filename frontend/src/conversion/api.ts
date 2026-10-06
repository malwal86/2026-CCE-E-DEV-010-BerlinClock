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

export function rethrowUnlessApiProblem(error: unknown): asserts error is ApiProblem {
  if (!(error instanceof ApiProblem)) throw error
}

/**
 * Sends a request to the API. A refusal comes back as its problem detail; a backend that cannot be reached, or an
 * error that is not a problem detail (such as the proxy's 502 page), becomes a problem saying `unavailable`.
 */
async function send(path: string, unavailable: string, init?: RequestInit): Promise<Response> {
  const backendUnavailable = (status: number) => new ApiProblem({ title: 'Backend unavailable', status, detail: unavailable })
  let response: Response
  try {
    response = await fetch(url(path), init)
  } catch {
    throw backendUnavailable(503)
  }
  if (response.ok) return response
  if (response.headers.get('Content-Type')?.startsWith('application/problem+json')) {
    throw new ApiProblem(await response.json())
  }
  throw backendUnavailable(response.status)
}

const UNAVAILABLE = 'Backend unavailable. Please try again.'

export async function convertTime(time: string): Promise<Conversion> {
  const response = await send('/api/conversions', 'Backend unavailable. Your conversion was not saved.', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ time }),
  })
  return response.json()
}

export async function fetchConversion(id: number): Promise<Conversion> {
  const response = await send(`/api/conversions/${id}`, UNAVAILABLE)
  return response.json()
}

export async function clearHistory(): Promise<void> {
  await send('/api/conversions', UNAVAILABLE, { method: 'DELETE' })
}

export async function fetchRecentConversions(): Promise<Conversion[]> {
  const response = await send('/api/conversions', UNAVAILABLE)
  return response.json()
}
