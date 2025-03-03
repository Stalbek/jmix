package io.jmix.flowui.action.view;

import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.Messages;
import io.jmix.flowui.FlowuiViewProperties;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.kit.component.FlowuiComponentUtils;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.StandardOutcome;
import io.jmix.flowui.view.ViewControllerUtils;
import org.springframework.beans.factory.annotation.Autowired;

@ActionType(DetailCloseAction.ID)
public class DetailCloseAction<E> extends OperationResultViewAction<DetailCloseAction<E>, StandardDetailView<E>> {

    public static final String ID = "detail_close";

    public DetailCloseAction() {
        this(ID);
    }

    public DetailCloseAction(String id) {
        super(id);
    }

    @Override
    protected void initAction() {
        super.initAction();

        this.icon = FlowuiComponentUtils.convertToIcon(VaadinIcon.BAN);
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.Cancel");
    }

    @Autowired
    protected void setFlowUiViewProperties(FlowuiViewProperties flowUiViewProperties) {
        this.shortcutCombination = KeyCombination.create(flowUiViewProperties.getCloseShortcut());
    }

    @Override
    public void execute() {
        checkTarget();

        operationResult = target.close(ViewControllerUtils.isCommitActionPerformed(target)
                ? StandardOutcome.COMMIT
                : StandardOutcome.CLOSE);

        super.execute();
    }
}
