import api from './api';

const getOwnerId = () => {
  const user = JSON.parse(localStorage.getItem('user'));
  if (!user?.id) throw new Error('User ID not found');
  return user.id;
};

export const getAllFunctions = async () => {
  const ownerId = getOwnerId();
  const res = await api.get('/tabulated-functions', {
    headers: { 'X-User-Id': ownerId }
  });
  return res.data;
};

export const getFunctionById = async (id) => {
  const ownerId = getOwnerId();
  const res = await api.get(`/tabulated-functions/${id}`, {
    headers: { 'X-User-Id': ownerId }
  });
  return res.data;
};

export const createFunctionFromPoints = async ({ name, xValues, yValues }) => {
  const ownerId = getOwnerId();
  const dto = {
    name,
    xValues,
    yValues,
    ownerId
  };
  const res = await api.post('/tabulated-functions/from-points', dto, {
    headers: { 'X-User-Id': ownerId }
  });
  return res.data;
};

export const createFunctionFromMath = async ({ name, mathFunctionType, xFrom, xTo, count }) => {
  const ownerId = getOwnerId();
  const dto = {
    name,
    mathFunctionType,
    xFrom,
    xTo,
    count,
    ownerId
  };
  const res = await api.post('/tabulated-functions/from-math', dto, {
    headers: { 'X-User-Id': ownerId }
  });
  return res.data;
};

export const updateFunctionName = async (id, name) => {
  const ownerId = getOwnerId();
  await api.patch(`/tabulated-functions/${id}/name`, { name }, {
    headers: { 'X-User-Id': ownerId }
  });
};

export const updateFunctionDataAndName = async (id, { xValues, yValues, name }) => {
  const ownerId = getOwnerId();
  const data = new TextEncoder().encode(JSON.stringify({ x: xValues, y: yValues }));
  await api.patch(`/tabulated-functions/${id}/data-and-name`, { name, data }, {
    headers: { 'X-User-Id': ownerId }
  });
};

export const deleteFunction = async (id) => {
  const ownerId = getOwnerId();
  await api.delete(`/tabulated-functions/${id}`, {
    headers: { 'X-User-Id': ownerId }
  });
};

export const differentiateFunction = async ({ functionId, resultName }) => {
  const ownerId = getOwnerId();
  const res = await api.post('/tabulated-functions/differentiate', {
    functionId,
    resultName,
    ownerId
  }, {
    headers: { 'X-User-Id': ownerId }
  });
  return res.data;
};

export const getFunctionsByUser = async (userId, sortBy = 'id', ascending = true) => {
  try {
    const allFunctions = await getAllFunctions();
    const userFunctions = allFunctions.filter(func =>
      func.ownerId === userId || func.userId === userId
    );

    if (sortBy === 'name') {
      userFunctions.sort((a, b) => {
        const comparison = a.name.localeCompare(b.name);
        return ascending ? comparison : -comparison;
      });
    } else if (sortBy === 'createdAt' || sortBy === 'date') {
      userFunctions.sort((a, b) => {
        const dateA = new Date(a.createdAt || a.date || 0);
        const dateB = new Date(b.createdAt || b.date || 0);
        return ascending ? dateA - dateB : dateB - dateA;
      });
    } else {
      userFunctions.sort((a, b) => ascending ? a.id - b.id : b.id - a.id);
    }

    return userFunctions;
  } catch (error) {
    console.error('Error in getFunctionsByUser:', error);
    throw error;
  }
};

export const getFunctionCountForUser = async (userId) => {
  try {
    const userFunctions = await getFunctionsByUser(userId);
    return userFunctions.length;
  } catch (error) {
    console.error('Error in getFunctionCountForUser:', error);
    return 0;
  }
};

export const createFunction = async (functionData) => {
  if (functionData.xValues && functionData.yValues) {
    return createFunctionFromPoints(functionData);
  } else if (functionData.mathFunctionType) {
    return createFunctionFromMath(functionData);
  }
  throw new Error('Invalid function data');
};

export const updateFunction = async (id, functionData) => {
  if (functionData.name) {
    return updateFunctionName(id, functionData.name);
  }
  throw new Error('Only name update is supported');
};

export const serializeFunction = (func) => {
  return JSON.stringify(func);
};

export const deserializeFunction = (str) => {
  return JSON.parse(str);
};

export const performOperation = async (operationData) => {
  const { operation, functionId1, functionId2, resultName } = operationData;

  try {
    const [func1, func2] = await Promise.all([
      getFunctionById(functionId1),
      getFunctionById(functionId2)
    ]);

    const resultPoints = func1.xValues.map((x, i) => {
      let y;
      switch (operation) {
        case 'add':
          y = func1.yValues[i] + func2.yValues[i];
          break;
        case 'subtract':
          y = func1.yValues[i] - func2.yValues[i];
          break;
        case 'multiply':
          y = func1.yValues[i] * func2.yValues[i];
          break;
        case 'divide':
          y = func2.yValues[i] !== 0 ? func1.yValues[i] / func2.yValues[i] : 0;
          break;
        default:
          y = func1.yValues[i];
      }
      return { x, y };
    });

    const resultFunc = await createFunctionFromPoints({
      name: resultName || `${operation}_result`,
      xValues: resultPoints.map(p => p.x),
      yValues: resultPoints.map(p => p.y)
    });

    return resultFunc;
  } catch (error) {
    console.error('Error performing operation:', error);
    throw error;
  }
};