package edu.ualberta.med.biobank.wizard;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import edu.ualberta.med.biobank.model.Capacity;
import edu.ualberta.med.biobank.model.ContainerCell;
import edu.ualberta.med.biobank.model.ContainerPosition;
import edu.ualberta.med.biobank.model.ContainerStatus;
import edu.ualberta.med.biobank.model.StorageContainer;
import edu.ualberta.med.biobank.widgets.ChooseStorageContainerWidget;

public abstract class AbstractContainerChooserPage extends WizardPage {

	private StorageContainer currentContainer;

	protected ChooseStorageContainerWidget containerWidget;

	protected Composite pageContainer;

	protected Text textPosition;

	protected Integer gridWidth;
	protected Integer gridHeight;

	protected int defaultDim1 = 6;
	protected int defaultDim2 = 8;

	public AbstractContainerChooserPage(String pageName) {
		super(pageName);
	}

	public AbstractContainerChooserPage(String pageName, String title,
			ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	@Override
	public void createControl(Composite parent) {
		pageContainer = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout(2, false);
		pageContainer.setLayout(layout);
		initComponent();
		setControl(pageContainer);
		setPageComplete(false);
		updateFreezerGrid();
	}

	protected void initComponent() {
		Composite gridParent = new Composite(pageContainer, SWT.NONE);
		gridParent.setLayout(new GridLayout(1, false));
		GridData gd = new GridData();
		gd.horizontalAlignment = SWT.CENTER;
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = 2;
		gridParent.setLayoutData(gd);
		containerWidget = new ChooseStorageContainerWidget(gridParent);
		containerWidget.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				positionSelection(e);
			}
		});

		Label label = new Label(pageContainer, SWT.NONE);
		label.setText("Choosen position:");
		textPosition = new Text(pageContainer, SWT.READ_ONLY | SWT.BORDER
				| SWT.SINGLE);
	}

	protected ContainerCell positionSelection(MouseEvent e) {
		ContainerCell cell = containerWidget.getPositionAtCoordinates(e.x, e.y);
		if (cell != null) {
			ContainerPosition cp = cell.getPosition();
			if (cp != null && cp.getOccupiedContainer() != null) {
				textPosition.setText(cp.getOccupiedContainer().getBarcode());
				setPageComplete(true);
			}
		} else {
			textPosition.setText("");
			setPageComplete(false);
		}
		return cell;
	}

	protected void updateFreezerGrid() {
		int dim1;
		int dim2;
		if (currentContainer == null) {
			dim1 = defaultDim1;
			dim2 = defaultDim2;
		} else {
			Capacity capacity = currentContainer.getStorageType().getCapacity();
			dim1 = capacity.getDimensionOneCapacity();
			dim2 = capacity.getDimensionTwoCapacity();
		}
		int width;
		if (gridWidth == null) {
			width = pageContainer.getSize().x - 13;
		} else {
			width = gridWidth;
		}
		int height;
		if (gridHeight == null) {
			height = 300;
		} else {
			height = gridHeight;
		}
		containerWidget.setGridSizes(dim1, dim2, width, height);
		if (currentContainer != null) {
			ContainerCell[][] cells = new ContainerCell[dim1][dim2];
			for (ContainerPosition position : currentContainer
				.getOccupiedPositions()) {
				int positionDim1 = position.getPositionDimensionOne() - 1;
				int positionDim2 = position.getPositionDimensionTwo() - 1;
				ContainerCell cell = new ContainerCell(position);
				StorageContainer occupiedContainer = position
					.getOccupiedContainer();
				Boolean full = occupiedContainer.getFull();
				if (full == null) {
					full = Boolean.FALSE;
				}
				int total = 0;
				if (!full) {
					// check if we can add a palette in the hotel
					if (occupiedContainer.getOccupiedPositions() != null) {
						total = occupiedContainer.getOccupiedPositions().size();
					}
					int capacityTotal = occupiedContainer.getStorageType()
						.getCapacity().getDimensionOneCapacity()
							* occupiedContainer.getStorageType().getCapacity()
								.getDimensionTwoCapacity();
					full = (total == capacityTotal);
				}
				if (full) {
					cell.setStatus(ContainerStatus.FILLED);
				} else if (total == 0) {
					cell.setStatus(ContainerStatus.EMPTY);
				} else {
					cell.setStatus(ContainerStatus.FREE_POSITIONS);
				}
				cells[positionDim1][positionDim2] = cell;
			}
			containerWidget.setContainersStatus(cells);
		}
		containerWidget.setVisible(true);
	}

	public void setCurrentStorageContainer(StorageContainer container) {
		this.currentContainer = container;
	}

}