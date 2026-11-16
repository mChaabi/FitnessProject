import { toWaNumber } from './whatsapp';

describe('toWaNumber', () => {
  it('normalises Moroccan and international formats', () => {
    expect(toWaNumber('0612345678')).toBe('212612345678');
    expect(toWaNumber('+212 6 12 34 56 78')).toBe('212612345678');
    expect(toWaNumber('06 12-34-56-78')).toBe('212612345678');
    expect(toWaNumber('00212612345678')).toBe('212612345678');
    expect(toWaNumber('612345678')).toBe('212612345678');
    expect(toWaNumber('+212 (0) 612345678')).toBe('212612345678');
    expect(toWaNumber('+33 6 12 34 56 78')).toBe('33612345678');
  });
  it('rejects values that cannot be a phone number', () => {
    expect(toWaNumber('123')).toBeNull();
    expect(toWaNumber('abc')).toBeNull();
    expect(toWaNumber(null)).toBeNull();
  });
});