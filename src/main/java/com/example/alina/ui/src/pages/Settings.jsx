import React, { useState } from 'react';
import { useFactory } from '../context/FactoryContext';
import Navbar from '../components/Navbar';
import Alert from '../components/Alert';
import './Settings.css';

const Settings = () => {
  const { factoryType, setFactoryType } = useFactory();
  const [success, setSuccess] = useState('');

  const handleSave = () => {
    setSuccess('Настройки успешно сохранены!');
    setTimeout(() => setSuccess(''), 3000);
  };

  return (
    <>
      <Navbar />
      <div className="settings-page">
      <div className="settings-container">
        <h1>Настройки</h1>

        <div className="settings-section">
          <h2>Фабрика табулированных функций</h2>
          <p className="settings-description">
            Выберите тип фабрики, которая будет использоваться по умолчанию
            для создания новых табулированных функций.
          </p>

          <div className="factory-options">
            <label className="factory-option">
              <input
                type="radio"
                name="factory"
                value="ARRAY"
                checked={factoryType === 'ARRAY'}
                onChange={(e) => setFactoryType(e.target.value)}
              />
              <div className="option-content">
                <h3>Массив (Array)</h3>
                <p>Реализация на основе массива. Быстрый доступ по индексу, фиксированный размер.</p>
              </div>
            </label>

            <label className="factory-option">
              <input
                type="radio"
                name="factory"
                value="LINKED_LIST"
                checked={factoryType === 'LINKED_LIST'}
                onChange={(e) => setFactoryType(e.target.value)}
              />
              <div className="option-content">
                <h3>Связный список (Linked List)</h3>
                <p>Реализация на основе связного списка. Динамический размер, эффективная вставка/удаление.</p>
              </div>
            </label>
          </div>

          <button className="btn-primary" onClick={handleSave}>
            Сохранить настройки
          </button>
        </div>

        <Alert message={success} type="success" onClose={() => setSuccess('')} />
      </div>
      </div>
    </>
  );
};

export default Settings;
