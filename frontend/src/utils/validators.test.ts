import { describe, expect, it } from 'vitest';
import { canOuting, isValidPhone } from '@/utils/validators';

describe('validators', () => {
  it('should validate phone format', () => {
    expect(isValidPhone('13812345678')).toBe(true);
    expect(isValidPhone('123456')).toBe(false);
  });

  it('should only allow resident customer outing', () => {
    expect(
      canOuting({
        id: 1,
        name: '张三',
        age: 70,
        gender: 1,
        status: 'RESIDENT'
      })
    ).toBe(true);

    expect(
      canOuting({
        id: 2,
        name: '李四',
        age: 72,
        gender: 2,
        status: 'DISCHARGED'
      })
    ).toBe(false);
  });
});
