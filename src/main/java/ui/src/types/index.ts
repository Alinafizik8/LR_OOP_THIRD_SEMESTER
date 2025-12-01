export interface User {
  id?: number;
  username: string;
  password?: string;
  role: 'user' | 'admin';
}

export interface Point {
  x: number;
  y: number;
}

export interface TabulatedFunction {
  id?: number;
  name: string;
  points: Point[];
  type: 'TABULATED';
  createdAt?: string;
  updatedAt?: string;
}

export interface MathFunctionMeta {
  key: string;          // "sin(x)" → SineFunction
  localized: string;    // "Синус"
  priority: number;     // 10 → выше в списке
}

export interface CompositeFunctionNode {
  id: string;
  type: 'primitive' | 'composite';
  name: string;
  children?: CompositeFunctionNode[];
}

export type Operation = 'ADD' | 'SUBTRACT' | 'MULTIPLY' | 'DIVIDE';