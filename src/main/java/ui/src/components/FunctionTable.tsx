import { Table, TextInput, Group, ActionIcon } from '@mantine/core';
import { IconTrash } from '@tabler/icons-react';
import { Point } from '../types';

interface FunctionTableProps {
  points: Point[];
  editable?: boolean;
  onPointsChange?: (points: Point[]) => void;
  removable?: boolean;
  onRemovePoint?: (index: number) => void;
}

export default function FunctionTable({
  points,
  editable = false,
  onPointsChange,
  removable = false,
  onRemovePoint,
}: FunctionTableProps) {
  const handleYChange = (index: number, y: string) => {
    if (!onPointsChange) return;
    const newPoints = [...points];
    const numY = parseFloat(y);
    if (!isNaN(numY)) {
      newPoints[index] = { ...newPoints[index], y: numY };
      onPointsChange(newPoints);
    }
  };

  return (
    <Table verticalSpacing="xs" highlightOnHover withTableBorder>
      <thead>
        <tr>
          <th>x</th>
          <th>y</th>
          {removable && <th></th>}
        </tr>
      </thead>
      <tbody>
        {points.map((point, i) => (
          <tr key={i}>
            <td>
              <TextInput
                value={point.x}
                readOnly
                size="xs"
                variant="unstyled"
              />
            </td>
            <td>
              <TextInput
                value={point.y}
                onChange={(e) => handleYChange(i, e.target.value)}
                size="xs"
                disabled={!editable}
              />
            </td>
            {removable && (
              <td>
                <ActionIcon color="red" variant="subtle" onClick={() => onRemovePoint?.(i)}>
                  <IconTrash size={16} />
                </ActionIcon>
              </td>
            )}
          </tr>
        ))}
      </tbody>
    </Table>
  );
}