/** A lamp in the kata notation: Y = yellow lit, R = red lit, O = off. */
export type Lamp = 'Y' | 'R' | 'O'

/** The rows of a Berlin Clock, exactly as the API returns them. */
export interface BerlinClockRows {
  seconds: Lamp
}

export interface Conversion extends BerlinClockRows {
  id: number
  time: string
  convertedAt: string
}
