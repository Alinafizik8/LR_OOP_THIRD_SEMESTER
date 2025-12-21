import React, { useState, useEffect } from 'react';
import Navbar from '../components/Navbar';
import FunctionChart2 from '../components/FunctionChart2';
import { useFactory } from '../context/FactoryContext';
import { functions } from '../services/api';
import './Differentiation.css';

const Differentiation = () => {
  const { factoryType } = useFactory();
  const [userFunctions, setUserFunctions] = useState([]);
  const [selectedFunctionId, setSelectedFunctionId] = useState('');
  const [sourceFunction, setSourceFunction] = useState(null);
  const [derivedFunction, setDerivedFunction] = useState(null);
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

  const handleFunctionSelect = async (e) => {
    const functionId = e.target.value;
    setSelectedFunctionId(functionId);
    setDerivedFunction(null);
    setError('');

    if (functionId) {
      try {
        const response = await functions.getById(functionId);
        setSourceFunction(response.data);
      } catch (err) {
        setError('Ошибка загрузки функции');
        console.error(err);
      }
    } else {
      setSourceFunction(null);
    }
  };

  const handleDifferentiate = async () => {
    if (!selectedFunctionId) {
      setError('Выберите функцию для дифференцирования');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const response = await functions.differentiate(
        parseInt(selectedFunctionId),
        factoryType
      );
      setDerivedFunction(response.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Ошибка при дифференцировании');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <Navbar />
      <div className="differentiation-page">
        <div className="differentiation-container">
          <h1>Дифференцирование функций</h1>
          <p className="page-description">
            Выберите табулированную функцию и получите её производную, вычисленную численными методами.
            Используется выбранная фабрика: <strong>{factoryType === 'ARRAY' ? 'Массив' : 'Связный список'}</strong>
          </p>

          {error && <div className="error-message">{error}</div>}

          <div className="diff-controls">
            <div className="control-group">
              <label htmlFor="function-select">Выберите функцию:</label>
              <select
                id="function-select"
                value={selectedFunctionId}
                onChange={handleFunctionSelect}
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

            <button
              onClick={handleDifferentiate}
              disabled={!selectedFunctionId || loading}
              className="differentiate-btn"
            >
              {loading ? 'Вычисление...' : 'Вычислить производную'}
            </button>
            <div className="control-group">
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

          {sourceFunction && (
            <div className="function-display">
              <h2>Исходная функция: {sourceFunction.name}</h2>
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
                      {sourceFunction.xValues.map((x, index) => (
                        <tr key={index}>
                          <td>{x.toFixed(4)}</td>
                          <td>{sourceFunction.yValues[index].toFixed(4)}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
                <div className="function-chart-wrapper">
                  <FunctionChart2 data={sourceFunction} coordinateSystem={coordinateSystem}/>
                </div>
              </div>
            </div>
          )}

          {derivedFunction && (
            <div className="function-display derived">
              <h2>Производная: {derivedFunction.name}</h2>
              <div className="function-content">
                <div className="function-table-wrapper">
                  <table className="function-table">
                    <thead>
                      <tr>
                        <th>X</th>
                        <th>Y' (производная)</th>
                      </tr>
                    </thead>
                    <tbody>
                      {derivedFunction.xValues.map((x, index) => (
                        <tr key={index}>
                          <td>{x.toFixed(4)}</td>
                          <td>{derivedFunction.yValues[index].toFixed(4)}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
                <div className="function-chart-wrapper">
                  <FunctionChart2 data={derivedFunction} coordinateSystem={coordinateSystem} />
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </>
  );
};

export default Differentiation;
