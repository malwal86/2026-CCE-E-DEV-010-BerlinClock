import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { LampRow } from './LampRow'

describe('LampRow', () => {
  it('renders one lamp per character, left to right, each with its colour', () => {
    render(<LampRow lamps="RRYO" testId="row" />)

    const lamps = Array.from(screen.getByTestId('row').children)
    expect(lamps.map((lamp) => lamp.getAttribute('data-lamp'))).toEqual(['R', 'R', 'Y', 'O'])
  })
})
