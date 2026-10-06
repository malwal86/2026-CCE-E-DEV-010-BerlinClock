import { describe, expect, it } from 'vitest'
import { conversionIdIn, conversionPath } from './route'

describe('conversion routes', () => {
  it('reads back the id of a conversion page', () => {
    expect(conversionIdIn(conversionPath(42))).toBe(42)
  })

  it.each(['/', '/conversions', '/conversions/', '/conversions/abc', '/conversions/-1', '/conversions/1.5', '/conversions/1/x'])(
    'finds no conversion in %s',
    (pathname) => {
      expect(conversionIdIn(pathname)).toBeUndefined()
    },
  )

  it('finds no conversion in an id too large to have been saved', () => {
    expect(conversionIdIn('/conversions/100000000000000000000')).toBeUndefined()
  })
})
