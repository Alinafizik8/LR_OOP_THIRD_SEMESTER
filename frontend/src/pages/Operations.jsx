import React, { useState, useEffect } from 'react';
import Navbar from '../components/Navbar';
import FunctionChart2 from '../components/FunctionChart2';
import { useFactory } from '../context/FactoryContext';
import { functions } from '../services/api';
import './Operations.css';

const Operations = () => {
  const { factoryType } = useFactory();
  const [userFunctions, setUserFunctions] = useState([]);
  const [firstFunctionId, setFirstFunctionId] = useState('');
  const [secondFunctionId, setSecondFunctionId] = useState('');
  const [firstFunction, setFirstFunction] = useState(null);
  const [secondFunction, setSecondFunction] = useState(null);
  const [resultFunction, setResultFunction] = useState(null);
  const [selectedOperation, setSelectedOperation] = useState('PLUS');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [coordinateSystem, setCoordinateSystem] = useState('cartesian');

  useEffect(() => {
    loadFunctions();
  }, []);

  const loadFunctions = async () => {
    try {
      const response = await functions.getAll();
      setUserFunctions(response.data);
    } catch (err) {
      setError('Ошибка загрузки функций');
      console.error(err);
    }
  };

  const handleFirstFunctionSelect = async (e) => {
    const functionId = e.target.value;
    setFirstFunctionId(functionId);
    setResultFunction(null);
    setError('');

    if (functionId) {
      try {
        const response = await functions.getById(functionId);
        setFirstFunction(response.data);
      } catch (err) {
        setError('Ошибка загрузки первой функции');
        console.error(err);
      }
    } else {
      setFirstFunction(null);
    }
  };

  const handleSecondFunctionSelect = async (e) => {
    const functionId = e.target.value;
    setSecondFunctionId(functionId);
    setResultFunction(null);
    setError('');

    if (functionId) {
      try {
        const response = await functions.getById(functionId);
        setSecondFunction(response.data);
      } catch (err) {
        setError('Ошибка загрузки второй функции');
        console.error(err);
      }
    } else {
      setSecondFunction(null);
    }
  };

  const handleOperation = async (operation) => {
    if (!firstFunctionId || !secondFunctionId) {
      setError('Выберите обе функции для выполнения операции');
      return;
    }

    setLoading(true);
    setError('');
    setSelectedOperation(operation);

    try {
      const response = await functions.operate(
        parseInt(firstFunctionId),
        parseInt(secondFunctionId),
        operation,
        factoryType
      );
      setResultFunction(response.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Ошибка при выполнении операции');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const getOperationSymbol = (operation) => {
    switch (operation) {
      case 'PLUS': return '+';
      case 'MINUS': return '-';
      case 'MULTIPLY': return '×';
      case 'DIVIDE': return '÷';
      default: return '';
    }
  };

  return (
    <>
      <Navbar />
      <div className="operations-page">
        <div className="operations-container">
          <h1>Операции над функциями</h1>
          <p className="page-description">
            Выполняйте поэлементные операции над двумя табулированными функциями.
            Используется выбранная фабрика: <strong>{factoryType === 'ARRAY' ? 'Массив' : 'Связный список'}</strong>
          </p>

          {error && <div className="error-message">{error}</div>}

          <div className="operations-controls">
            <div className="function-selectors">
              <div className="selector-group">
                <label htmlFor="first-function">Первая функция:</label>
                <select
                  id="first-function"
                  value={firstFunctionId}
                  onChange={handleFirstFunctionSelect}
                  className="function-select"
                >
                  <option value="">-- Выберите функцию --</option>
                  {userFunctions.map((func) => (
                    <option key={func.id} value={func.id}>
                      {func.name} ({func.count} точек)
                    </option>
                  ))}
                </select>
              </div>

              <div className="selector-group">
                <label htmlFor="second-function">Вторая функция:</label>
                <select
                  id="second-function"
                  value={secondFunctionId}
                  onChange={handleSecondFunctionSelect}
                  className="function-select"
                >
                  <option value="">-- Выберите функцию --</option>
                  {userFunctions.map((func) => (
                    <option key={func.id} value={func.id}>
                      {func.name} ({func.count} точек)
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <div className="operation-buttons">
              <h3>Выберите операцию:</h3>
              <div className="button-group">
                <button
                  onClick={() => handleOperation('PLUS')}
                  disabled={!firstFunctionId || !secondFunctionId || loading}
                  className="operation-btn plus"
                >
                  {loading && selectedOperation === 'PLUS' ? 'Вычисление...' : 'Сложение (+)'}
                </button>
                <button
                  onClick={() => handleOperation('MINUS')}
                  disabled={!firstFunctionId || !secondFunctionId || loading}
                  className="operation-btn minus"
                >
                  {loading && selectedOperation === 'MINUS' ? 'Вычисление...' : 'Вычитание (-)'}
                </button>
                <button
                  onClick={() => handleOperation('MULTIPLY')}
                  disabled={!firstFunctionId || !secondFunctionId || loading}
                  className="operation-btn multiply"
                >
                  {loading && selectedOperation === 'MULTIPLY' ? 'Вычисление...' : 'Умножение (×)'}
                </button>
                <button
                  onClick={() => handleOperation('DIVIDE')}
                  disabled={!firstFunctionId || !secondFunctionId || loading}
                  className="operation-btn divide"
                >
                  {loading && selectedOperation === 'DIVIDE' ? 'Вычисление...' : 'Деление (÷)'}
                </button>
              </div>
            </div>
            <div className="selector-group">
              <label>Система координат</label>
              <select
                value={coordinateSystem}
                onChange={(e) => setCoordinateSystem(e.target.value)}
                className="function-select"
              >
                <option value="cartesian">Декартова</option>
                <option value="polar">Полярная</option>
                <option value="cylindrical">Цилиндрическая</option>
                <option value="spherical">Сферическая</option>
              </select>
            </div>
          </div>

          <div className="functions-display">
            {firstFunction && (
              <div className="function-display">
                <h2>Функция 1: {firstFunction.name}</h2>
                <div className="function-content">
                  <div className="function-table-wrapper">
                    <table className="function-table">
                      <thead>
                        <tr>
                          <th>X</th>
                          <th>Y</th>
                        </tr>
                      </thead>
                      <tbody>
                        {firstFunction.xValues.map((x, index) => (
                          <tr key={index}>
                            <td>{x.toFixed(4)}</td>
                            <td>{firstFunction.yValues[index].toFixed(4)}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                  <div className="function-chart-wrapper">
                    <FunctionChart2 data={firstFunction} coordinateSystem={coordinateSystem}/>
                  </div>
                </div>
              </div>
            )}

            {secondFunction && (
              <div className="function-display">
                <h2>Функция 2: {secondFunction.name}</h2>
                <div className="function-content">
                  <div className="function-table-wrapper">
                    <table className="function-table">
                      <thead>
                        <tr>
                          <th>X</th>
                          <th>Y</th>
                        </tr>
                      </thead>
                      <tbody>
                        {secondFunction.xValues.map((x, index) => (
                          <tr key={index}>
                            <td>{x.toFixed(4)}</td>
                            <td>{secondFunction.yValues[index].toFixed(4)}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                  <div className="function-chart-wrapper">
                    <FunctionChart2 data={secondFunction} coordinateSystem={coordinateSystem} />
                  </div>
                </div>
              </div>
            )}
          </div>

          {resultFunction && (
            <div className="result-section">
              <div className="function-display result">
                <h2>Результат: {resultFunction.name}</h2>
                <div className="function-content">
                  <div className="function-table-wrapper">
                    <table className="function-table">
                      <thead>
                        <tr>
                          <th>X</th>
                          <th>Y (результат)</th>
                        </tr>
                      </thead>
                      <tbody>
                        {resultFunction.xValues.map((x, index) => (
                          <tr key={index}>
                            <td>{x.toFixed(4)}</td>
                            <td>{resultFunction.yValues[index].toFixed(4)}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                  <div className="function-chart-wrapper">
                    <FunctionChart2 data={resultFunction} coordinateSystem={coordinateSystem} />
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </>
  );
};

export default Operations;
