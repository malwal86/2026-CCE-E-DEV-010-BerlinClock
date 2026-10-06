const CONVERSION_PATH = /^\/conversions\/(\d+)$/

/** The page that shows one conversion, served by the client: nginx and Vite fall back to index.html. */
export const conversionPath = (id: number) => `/conversions/${id}`

export function conversionIdIn(pathname: string): number | undefined {
  const id = Number(CONVERSION_PATH.exec(pathname)?.[1])
  return Number.isSafeInteger(id) ? id : undefined
}
