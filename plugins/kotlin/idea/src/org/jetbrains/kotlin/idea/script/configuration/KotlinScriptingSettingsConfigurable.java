// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.kotlin.idea.script.configuration;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.options.UnnamedConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.panels.VerticalLayout;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.idea.base.resources.KotlinBundle;
import org.jetbrains.kotlin.idea.core.script.ScriptDefinitionsManager;
import org.jetbrains.kotlin.idea.core.script.settings.KotlinScriptingSettings;
import org.jetbrains.kotlin.scripting.definitions.ScriptDefinition;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

import static org.jetbrains.kotlin.idea.core.script.ScriptUtilsKt.getAllDefinitions;

public class KotlinScriptingSettingsConfigurable implements SearchableConfigurable {
    public static final String ID = "preferences.language.Kotlin.scripting";

    private JPanel root;
    private JPanel panelScriptDefinitionsChooser;
    private JPanel additionalSettingsPanel;

    private final List<UnnamedConfigurable> scriptingSupportSettingsConfigurables = new ArrayList<>();

    private final KotlinScriptDefinitionsModel model;

    private final Project project;
    private final ScriptDefinitionsManager manager;
    private final KotlinScriptingSettings settings;

    public KotlinScriptingSettingsConfigurable(Project project) {
        this.project = project;
        manager = ScriptDefinitionsManager.Companion.getInstance(project);
        settings = KotlinScriptingSettings.Companion.getInstance(project);
        model = KotlinScriptDefinitionsModel.Companion.createModel(getAllDefinitions(project), settings);
    }

    @Override
    public @Nullable JComponent createComponent() {
        panelScriptDefinitionsChooser.setLayout(new VerticalLayout(0));

        panelScriptDefinitionsChooser.add(new TitledSeparator(KotlinBundle.message("kotlin.script.definitions.title")));

        JLabel commentLabel = new JLabel(KotlinBundle.message("kotlin.script.definitions.model.name.autoReloadScriptDependencies.description"));
        commentLabel.setForeground(UIUtil.getContextHelpForeground());
        panelScriptDefinitionsChooser.add(commentLabel);

        TableView<ModelDescriptor> table = new TableView<>(model);
        panelScriptDefinitionsChooser.add(
                ToolbarDecorator.createDecorator(table)
                        .disableAddAction()
                        .disableRemoveAction()
                        .createPanel()
        );
        table.setVisibleRowCount(12);

        additionalSettingsPanel.setLayout(new VerticalLayout(0));

        List<ScriptingSupportSpecificSettingsProvider> providers =
                ScriptingSupportSpecificSettingsProvider.SETTINGS_PROVIDERS.getExtensionList(project);
        for (ScriptingSupportSpecificSettingsProvider provider : providers) {
            additionalSettingsPanel.add(new TitledSeparator(provider.getTitle()));

            UnnamedConfigurable configurable = provider.createConfigurable();
            scriptingSupportSettingsConfigurables.add(configurable);

            additionalSettingsPanel.add(configurable.createComponent());
        }
        return root;
    }

    @Override
    public boolean isModified() {
        for (UnnamedConfigurable supportSpecificSetting : scriptingSupportSettingsConfigurables) {
            if (supportSpecificSetting.isModified()) {
                return true;
            }
        }

        return isScriptDefinitionsChanged();
    }

    @Override
    public void apply() throws ConfigurationException {
        if (isScriptDefinitionsChanged()) {
            for (ModelDescriptor item : model.getItems()) {
                ScriptDefinition definition = item.getDefinition();
                int order = model.getItems().indexOf(item);
                settings.setOrder(definition, order);
                settings.setEnabled(definition, item.isEnabled());
                settings.setAutoReloadConfigurations(definition, item.getAutoReloadConfigurations());
            }

            manager.reorderDefinitions();
        }

        for (UnnamedConfigurable supportSpecificSetting : scriptingSupportSettingsConfigurables) {
            supportSpecificSetting.apply();
        }
    }

    @Override
    public void reset() {
        model.setDefinitions(manager.getAllDefinitions(), settings);
        for (UnnamedConfigurable supportSpecificSetting : scriptingSupportSettingsConfigurables) {
            supportSpecificSetting.reset();
        }
    }

    private boolean isScriptDefinitionsChanged() {
        for (ModelDescriptor item : model.getItems()) {
            if (settings.isScriptDefinitionEnabled(item.getDefinition()) != item.isEnabled()) {
                return true;
            }
            if (settings.autoReloadConfigurations(item.getDefinition()) != item.getAutoReloadConfigurations()) {
                return true;
            }
        }
        return !model.getDefinitions().equals(manager.getAllDefinitions());
    }


    @Override
    @Nls
    public String getDisplayName() {
        return KotlinBundle.message("script.name.kotlin.scripting");
    }

    @Override
    public @NotNull String getId() {
        return ID;
    }
}
