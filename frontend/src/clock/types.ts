/** A lamp in the kata notation: Y = yellow lit, R = red lit, O = off. */
export type Lamp = 'Y' | 'R' | 'O'

/** The rows of a Berlin Clock, exactly as the API returns them. */
export interface BerlinClockRows {
  seconds: Lamp
  /** Four red lamps, in the kata notation (e.g. "RRRO"). */
  fiveHours: string
  /** Four red lamps, in the kata notation (e.g. "RRRR"). */
  singleHours: string
  /** Eleven lamps in the kata notation, yellow with red quarter markers (e.g. "YYRYYRYOOOO"). */
  fiveMinutes: string
  /** Four yellow lamps, in the kata notation (e.g. "YYOO"). */
  singleMinutes: string
  /** The entire clock as one 24-character code, every lamp from top to bottom (e.g. "YRRROROOOYYRYYRYYRYOOOOO"). */
  clock: string
}
