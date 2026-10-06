const CONVERSION_PATH = /^\/conversions\/(\d+)$/

/** The page that shows one conversion, served by the client: nginx and Vite fall back to index.html. */
export const conversionPath = (id: number) => `/conversions/${id}`

/** The id of the conversion a path shows, if it is a conversion page. */
export function conversionIdIn(pathname: string): number | undefined {
  const match = CONVERSION_PATH.exec(pathname)
  return match ? Number(match[1]) : undefined
}
