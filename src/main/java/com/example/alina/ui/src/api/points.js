import api from './api';

const getOwnerId = () => {
  const user = JSON.parse(localStorage.getItem('user'));
  if (!user?.id) throw new Error('User ID not found');
  return user.id;
};

// Существующие функции
export const loadPointsFromFile = async (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();

    reader.onload = (e) => {
      try {
        const content = e.target.result;
        const lines = content.split('\n');
        const points = [];

        for (let line of lines) {
          line = line.trim();
          if (!line || line.startsWith('#')) continue;

          const parts = line.split(/\s+/);
          if (parts.length >= 2) {
            const x = parseFloat(parts[0]);
            const y = parseFloat(parts[1]);

            if (!isNaN(x) && !isNaN(y)) {
              points.push({ x, y });
            }
          }
        }

        resolve(points);
      } catch (error) {
        reject(new Error('Error parsing file: ' + error.message));
      }
    };

    reader.onerror = () => reject(new Error('Error reading file'));
    reader.readAsText(file);
  });
};

export const validatePoints = (points) => {
  if (!Array.isArray(points) || points.length === 0) {
    return { valid: false, message: 'No points provided' };
  }

  for (let i = 0; i < points.length; i++) {
    const point = points[i];
    if (typeof point.x !== 'number' || isNaN(point.x) ||
        typeof point.y !== 'number' || isNaN(point.y)) {
      return { valid: false, message: `Invalid point at index ${i}` };
    }
  }

  // Проверяем уникальность X
  const xValues = points.map(p => p.x);
  const uniqueX = [...new Set(xValues)];
  if (uniqueX.length !== xValues.length) {
    return { valid: false, message: 'Duplicate X values found' };
  }

  return { valid: true, message: 'OK' };
};

// НОВЫЕ ФУНКЦИИ:

export const getPointsByFunctionId = async (functionId) => {
  try {
    // Получаем функцию по ID
    const funcRes = await api.get(`/tabulated-functions/${functionId}`, {
      headers: { 'X-User-Id': getOwnerId() }
    });

    const func = funcRes.data;

    // Преобразуем данные функции в точки
    if (func.xValues && func.yValues &&
        Array.isArray(func.xValues) && Array.isArray(func.yValues) &&
        func.xValues.length === func.yValues.length) {

      return func.xValues.map((x, index) => ({
        x,
        y: func.yValues[index],
        id: index
      }));
    }

    return [];
  } catch (error) {
    console.error('Error getting points by function ID:', error);
    return [];
  }
};

export const getPointsByFunctionName = async (functionName) => {
  try {
    // Получаем все функции
    const allFuncs = await api.get('/tabulated-functions', {
      headers: { 'X-User-Id': getOwnerId() }
    });

    // Ищем функцию по имени
    const func = allFuncs.data.find(f => f.name === functionName);
    if (!func) return [];

    return getPointsByFunctionId(func.id);
  } catch (error) {
    console.error('Error getting points by function name:', error);
    return [];
  }
};

export const getDifferentiatedPoints = async (functionId) => {
  try {
    // Дифференцируем функцию
    const diffRes = await api.post('/tabulated-functions/differentiate', {
      functionId,
      resultName: `diff_${functionId}`,
      ownerId: getOwnerId()
    }, {
      headers: { 'X-User-Id': getOwnerId() }
    });

    const diffFunc = diffRes.data;

    // Преобразуем в точки
    if (diffFunc.xValues && diffFunc.yValues) {
      return diffFunc.xValues.map((x, index) => ({
        x,
        y: diffFunc.yValues[index],
        id: index,
        isDerivative: true
      }));
    }

    return [];
  } catch (error) {
    console.error('Error getting differentiated points:', error);
    return [];
  }
};

export const createPointsBatch = async (functionId, points) => {
  try {
    // Создаем новую функцию с этими точками
    const xValues = points.map(p => p.x);
    const yValues = points.map(p => p.y);

    const funcRes = await api.post('/tabulated-functions/from-points', {
      name: `Points_${Date.now()}`,
      xValues,
      yValues,
      ownerId: getOwnerId()
    }, {
      headers: { 'X-User-Id': getOwnerId() }
    });

    return funcRes.data;
  } catch (error) {
    console.error('Error creating points batch:', error);
    throw error;
  }
};

export const updatePointsBatch = async (functionId, points) => {
  try {
    const xValues = points.map(p => p.x);
    const yValues = points.map(p => p.y);

    await api.patch(`/tabulated-functions/${functionId}/data-and-name`, {
      name: `Updated_${Date.now()}`,
      data: new TextEncoder().encode(JSON.stringify({ x: xValues, y: yValues }))
    }, {
      headers: { 'X-User-Id': getOwnerId() }
    });

    return { success: true };
  } catch (error) {
    console.error('Error updating points batch:', error);
    throw error;
  }
};

export const deletePoint = async (pointId) => {
  // В текущей архитектуре точки не имеют отдельных ID
  // Эта функция не может быть реализована без изменений в бэкенде
  console.warn('deletePoint not implemented - points are part of function data');
  return { success: true, message: 'Point deletion not supported in current API' };
};

export const interpolatePoint = async (functionId, x) => {
  try {
    const points = await getPointsByFunctionId(functionId);
    if (points.length === 0) return null;

    // Сортируем точки по X
    const sortedPoints = [...points].sort((a, b) => a.x - b.x);

    // Если x за пределами диапазона
    if (x <= sortedPoints[0].x) return sortedPoints[0].y;
    if (x >= sortedPoints[sortedPoints.length - 1].x) return sortedPoints[sortedPoints.length - 1].y;

    // Линейная интерполяция
    for (let i = 0; i < sortedPoints.length - 1; i++) {
      const x1 = sortedPoints[i].x;
      const x2 = sortedPoints[i + 1].x;
      const y1 = sortedPoints[i].y;
      const y2 = sortedPoints[i + 1].y;

      if (x >= x1 && x <= x2) {
        return y1 + (x - x1) * (y2 - y1) / (x2 - x1);
      }
    }

    return null;
  } catch (error) {
    console.error('Error interpolating point:', error);
    return null;
  }
};

// Экспорт по умолчанию
export default {
  loadPointsFromFile,
  validatePoints,
  getPointsByFunctionId,
  getPointsByFunctionName,
  getDifferentiatedPoints,
  createPointsBatch,
  updatePointsBatch,
  deletePoint,
  interpolatePoint
};