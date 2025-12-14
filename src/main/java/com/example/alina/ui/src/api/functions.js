import api from './api';

const getOwnerId = () => {
  const user = JSON.parse(localStorage.getItem('user'));
  if (!user?.id) throw new Error('User ID not found');
  return user.id;
};

export const getAllFunctions = async () => {
  const response = await api.get('/api/tabulated-functions');
  return response.data;
};


export const getFunctionById = async (id) => {
  console.log(`Getting function ${id}...`);
  const response = await api.get(`/api/tabulated-functions/${id}`);
  return response.data;
};

export const createFunctionFromPoints = async ({ name, xValues, yValues }) => {
  console.log('Creating function from points...');
  const dto = { name, xValues, yValues, ownerId: getOwnerId() };
  const response = await api.post('/api/tabulated-functions/from-points', dto);
  return response.data;
};

export const createFunctionFromMath = async ({ name, mathFunctionType, xFrom, xTo, count }) => {
  console.log('Creating math function...');
  const dto = { name, mathFunctionType, xFrom, xTo, count, ownerId: getOwnerId() };
  const response = await api.post('/api/tabulated-functions/from-math', dto);
  return response.data;
};

export const updateFunctionName = async (id, name) => {
  console.log(`Updating name for function ${id}...`);
  await api.patch(`/api/tabulated-functions/${id}/name`, { name });
};

export const updateFunctionDataAndName = async (id, { xValues, yValues, name }) => {
  console.log(`Updating data for function ${id}...`);
  const data = new TextEncoder().encode(JSON.stringify({ x: xValues, y: yValues }));
  await api.patch(`/api/tabulated-functions/${id}/data-and-name`, { name, data });
};

export const deleteFunction = async (id) => {
  console.log(`Deleting function ${id}...`);
  await api.delete(`/api/tabulated-functions/${id}`);
};

export const differentiateFunction = async ({ functionId, resultName }) => {
  console.log(`Differentiating function ${functionId}...`);
  const dto = { functionId, resultName, ownerId: getOwnerId() };
  const response = await api.post('/api/tabulated-functions/differentiate', dto);
  return response.data;
};

export const getFunctionsByUser = async (userId, sortBy = 'id', ascending = true) => {
  try {
    console.log(`Getting functions for user ${userId}...`);
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
    console.log(`Performing ${operation} on functions ${functionId1} + ${functionId2}...`);
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
