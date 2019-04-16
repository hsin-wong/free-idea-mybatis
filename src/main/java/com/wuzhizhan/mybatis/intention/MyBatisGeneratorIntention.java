package com.wuzhizhan.mybatis.intention;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.notification.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.*;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.exception.XMLParserException;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author hsin
 *
 * mybatis generator intention
 */
public class MyBatisGeneratorIntention implements IntentionAction {
    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getText() {
        return "MyBatis generator";
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getFamilyName() {
        return "MyBatis";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        return true;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        try {
            List<String> warnings = new ArrayList<>();
            boolean overwrite = true;
            String path = file.getVirtualFile().getCanonicalPath();
            File configFile = new File(path);
            ConfigurationParser cp = new ConfigurationParser(warnings);
            Configuration config = cp.parseConfiguration(configFile);
            for(Context context: config.getContexts()) {
                JavaClientGeneratorConfiguration clientConfig = context.getJavaClientGeneratorConfiguration();
                clientConfig.setTargetProject(project.getBasePath()+"/" +clientConfig.getTargetProject());

                SqlMapGeneratorConfiguration sqlConfig = context.getSqlMapGeneratorConfiguration();
                sqlConfig.setTargetProject(project.getBasePath()+"/"+sqlConfig.getTargetProject());

                JavaModelGeneratorConfiguration modelConfig = context.getJavaModelGeneratorConfiguration();
                modelConfig.setTargetProject(project.getBasePath()+"/"+modelConfig.getTargetProject());
            }
            DefaultShellCallback callback = new DefaultShellCallback(overwrite);
            MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
            myBatisGenerator.generate(null);
            for (String warning: warnings){
                showMyMessage(warning, NotificationType.WARNING);
            }

        } catch (SQLException | IOException | InterruptedException | InvalidConfigurationException | XMLParserException e) {
            showMyMessage(e.getMessage(), NotificationType.ERROR);
        }catch (Exception e){
            showMyMessage(e.getMessage(), NotificationType.ERROR);
        }
    }

    public static final NotificationGroup GROUP_DISPLAY_ID_INFO =
            new NotificationGroup("MyBatis group",
                    NotificationDisplayType.BALLOON, true);
    void showMyMessage(String message, NotificationType type) {
        ApplicationManager.getApplication().invokeLater(() -> {
                Notification notification = GROUP_DISPLAY_ID_INFO.createNotification(message, type);
                Project[] projects = ProjectManager.getInstance().getOpenProjects();
                Notifications.Bus.notify(notification, projects[0]);
        });
    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }
}
