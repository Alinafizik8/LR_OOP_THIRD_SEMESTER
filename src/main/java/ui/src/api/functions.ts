import api from './client';
import { TabulatedFunction, MathFunctionMeta, Operation } from '../types';

export const getFunctions = () => api.get<TabulatedFunction[]>('/v1/functions');
export const createFunction = (fn: Omit<TabulatedFunction, 'id'>) =>
  api.post<TabulatedFunction>('/v1/functions', fn);

export const updateFunction = (id: number, fn: Partial<TabulatedFunction>) =>
  api.put<TabulatedFunction>(`/v1/functions/${id}`, fn);

export const deleteFunction = (id: number) =>
  api.delete(`/v1/functions/${id}`);

export const getMathFunctions = () =>
  api.get<MathFunctionMeta[]>('/v1/functions/meta');

// Создание из MathFunction
export const createFromMathFunction = (
  name: string,
  mathFunctionKey: string,
  from: number,
  to: number,
  steps: number
) => api.post<TabulatedFunction>('/v1/functions/from-math', {
  name, mathFunctionKey, from, to, steps
});

// Операции
export const applyOperation = (
  op: Operation,
  leftId: number,
  rightId: number
) => api.post<TabulatedFunction>('/v1/operations', { op, leftId, rightId });

// Дифференцирование
export const differentiate = (id: number) =>
  api.post<TabulatedFunction>('/v1/differentiate', { id });

// Интеграл
export const integrate = (id: number, threads: number) =>
  api.post<number>('/v1/integrate', { id, threads });

// Composite — получение дерева
export const getCompositeFunctions = () =>
  api.get<CompositeFunctionNode[]>('/v1/composite');

export const createCompositeFunction = (name: string, root: CompositeFunctionNode) =>
  api.post('/v1/composite', { name, root });