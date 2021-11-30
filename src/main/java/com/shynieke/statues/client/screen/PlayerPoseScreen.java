package com.shynieke.statues.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.shynieke.statues.Reference;
import com.shynieke.statues.entity.PlayerStatue;
import com.shynieke.statues.packets.PlayerStatueSyncMessage;
import com.shynieke.statues.packets.StatuesNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PacketDistributor;

@OnlyIn(Dist.CLIENT)
public class PlayerPoseScreen extends Screen {
    private final PlayerStatue playerStatueEntity;
    private final PlayerStatueData playerStatueData;

    private final String[] buttonLabels = new String[] { "small", "rotation", "y_offset", "locked", "name_visible", "gravity" };
    private final String[] sliderLabels = new String[] { "head", "body", "left_leg", "right_leg", "left_arm", "right_arm" };

    private NumberFieldBox rotationTextField;
    private DecimalNumberFieldBox YOffsetTextField;
    private ToggleButton smallButton;
    private ToggleButton lockButton;
    private ToggleButton nameVisibleButton;
    private ToggleButton noGravityButton;
    private final NumberFieldBox[] poseTextFields = new NumberFieldBox[18];

    private Button doneButton;
    private Button cancelButton;

    public PlayerPoseScreen(PlayerStatue playerStatue) {
        super(NarratorChatListener.NO_TITLE);

        this.playerStatueEntity = playerStatue;

        this.playerStatueData = new PlayerStatueData();
        this.playerStatueData.readNBT(playerStatueEntity.saveWithoutId(new CompoundTag()));

        for (int i = 0; i < this.buttonLabels.length; i++)
            this.buttonLabels[i] = I18n.get(String.format("%s.playerstatue.gui.label." + this.buttonLabels[i], Reference.MOD_ID));
        for (int i = 0; i < this.sliderLabels.length; i++)
            this.sliderLabels[i] = I18n.get(String.format("%s.playerstatue.gui.label." + this.sliderLabels[i], Reference.MOD_ID));
    }

    public static void openScreen(PlayerStatue playerStatue) {
        Minecraft.getInstance().setScreen(new PlayerPoseScreen(playerStatue));
    }

    @Override
    protected void init() {
        super.init();

        int offsetX = 110;
        int offsetY = 50;

        this.smallButton = this.addRenderableWidget(new ToggleButton(offsetX, offsetY, 40, 20, this.playerStatueData.isSmall(), (button) -> {
            ToggleButton toggleButton = ((ToggleButton)button);
            toggleButton.setValue(!toggleButton.getValue());
            this.textFieldUpdated();
        }));
        this.lockButton = this.addRenderableWidget(new ToggleButton(offsetX, offsetY + 66, 40, 20, this.playerStatueData.isLocked(), (button) -> {
            ToggleButton toggleButton = ((ToggleButton)button);
            toggleButton.setValue(!toggleButton.getValue());
            this.textFieldUpdated();
        }));
        this.nameVisibleButton = this.addRenderableWidget(new ToggleButton(offsetX, offsetY + 89, 40, 20, this.playerStatueData.getNameVisible(), (button) -> {
            ToggleButton toggleButton = ((ToggleButton)button);
            toggleButton.setValue(!toggleButton.getValue());
            this.textFieldUpdated();
        }));
        this.noGravityButton = this.addRenderableWidget(new ToggleButton(offsetX, offsetY + 112, 40, 20, this.playerStatueData.hasNoGravity(), (button) -> {
            ToggleButton toggleButton = ((ToggleButton)button);
            toggleButton.setValue(!toggleButton.getValue());
            this.textFieldUpdated();
        }));

        // rotation textbox
        this.rotationTextField = new NumberFieldBox(this.font, 1 + offsetX, 1 + offsetY + (22), 38, 17, new TextComponent("field.rotation"));
        this.rotationTextField.setValue(String.valueOf((int)this.playerStatueData.rotation));
        this.rotationTextField.setMaxLength(3);
        this.addWidget(this.rotationTextField);

        // Y Offset textbox
        this.YOffsetTextField = new DecimalNumberFieldBox(this.font, 1 + offsetX, 1 + offsetY + (44), 38, 17, new TextComponent("field.yOffset"));
        this.YOffsetTextField.setValue(String.valueOf((float) Mth.clamp(this.playerStatueData.yOffset, -1, 1)));
        this.YOffsetTextField.setMaxLength(5);
        this.addWidget(this.YOffsetTextField);

        // pose textboxes
        offsetX = this.width - 20 - 100;
        for (int i = 0; i < this.poseTextFields.length; i++) {
            int x = 1 + offsetX + ((i % 3) * 35);
            int y = 1 + offsetY + ((i / 3) * 22);
            int width = 28;
            int height = 17;
            String value = String.valueOf((int)this.playerStatueData.pose[i]);

            this.poseTextFields[i] = new NumberFieldBox(this.font, x, y, width, height, new TextComponent(String.format("field.%s", i)));
            this.poseTextFields[i].setValue(value);
            this.poseTextFields[i].setMaxLength(3);
            this.addWidget(this.poseTextFields[i]);
        }

        offsetY = this.height / 4 + 120 + 12;

        // done & cancel buttons
        offsetX = this.width - 20;
        this.doneButton = this.addRenderableWidget(new Button(offsetX - ((2 * 96) + 2), offsetY, 96, 20, new TranslatableComponent("gui.done"), (button) -> {
            this.updateEntity(this.writeFieldsToNBT());
            this.minecraft.setScreen((Screen) null);
        }));
        this.cancelButton = this.addRenderableWidget(new Button(offsetX - 96, offsetY, 96, 20, new TranslatableComponent("gui.cancel"), (button) -> {
            this.updateEntity(this.playerStatueData.writeNBT());
            this.minecraft.setScreen((Screen) null);
        }));
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);

        // Draw gui title
        drawCenteredString(matrixStack, this.font, I18n.get(String.format("%s.playerstatue.gui.title", Reference.MOD_ID)), this.width / 2, 20, 0xFFFFFF);

        // Draw textboxes
        this.rotationTextField.render(matrixStack, mouseX, mouseY, partialTicks);
        this.YOffsetTextField.render(matrixStack, mouseX, mouseY, partialTicks);
        for (NumberFieldBox textField : this.poseTextFields)
            textField.render(matrixStack, mouseX, mouseY, partialTicks);

        int offsetY = 50;

        // left column labels
        int offsetX = 20;
        for (int i = 0; i < this.buttonLabels.length; i++) {
            int x = offsetX;
            int y = offsetY + (i * 22) + (10 - (this.font.lineHeight / 2));
            drawString(matrixStack, this.font, this.buttonLabels[i], x, y, 0xA0A0A0);
        }

        // right column labels
        offsetX = this.width - 20 - 100;
        // x, y, z
        drawString(matrixStack, this.font, "X", offsetX, 37, 0xA0A0A0);
        drawString(matrixStack, this.font, "Y", offsetX + (35), 37, 0xA0A0A0);
        drawString(matrixStack, this.font, "Z", offsetX + (2 * 35), 37, 0xA0A0A0);
        // pose textboxes
        for (int i = 0; i < this.sliderLabels.length; i++) {
            int x = offsetX - this.font.width(this.sliderLabels[i]) - 10;
            int y = offsetY + (i * 22) + (10 - (this.font.lineHeight / 2));
            drawString(matrixStack, this.font, this.sliderLabels[i], x, y, 0xA0A0A0);
        }

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void tick() {
        super.tick();
        this.rotationTextField.tick();
        this.YOffsetTextField.tick();
        for (NumberFieldBox textField : this.poseTextFields)
            textField.tick();
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        boolean typed = super.charTyped(codePoint, modifiers);
        if(typed) {
            this.textFieldUpdated();
        }
        return typed;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 15) { //Tab
            for (int i = 0; i < this.poseTextFields.length; i++) {
                if (this.poseTextFields[i].isFocused()) {
                    this.textFieldUpdated();
                    this.poseTextFields[i].moveCursorToEnd();
                    this.poseTextFields[i].setFocused(false);

                    int j = (!Screen.hasShiftDown() ? (i == this.poseTextFields.length - 1 ? 0 : i + 1) : (i == 0 ? this.poseTextFields.length - 1 : i - 1));
                    this.poseTextFields[j].setFocused(true);
                    this.poseTextFields[j].moveCursorTo(0);
                    this.poseTextFields[j].setHighlightPos(this.poseTextFields[j].getValue().length());
                }
            }
        } else {
            if (this.rotationTextField.keyPressed(keyCode, scanCode, modifiers)) {
                this.textFieldUpdated();
                return true;
            } else if (this.YOffsetTextField.keyPressed(keyCode, scanCode, modifiers)) {
                this.textFieldUpdated();
                return true;
            } else {
                for (NumberFieldBox textField : this.poseTextFields) {
                    if (textField.keyPressed(keyCode, scanCode, modifiers)) {
                        this.textFieldUpdated();
                        return true;
                    }
                }
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.rotationTextField.mouseClicked(mouseX, mouseY, button);
        this.YOffsetTextField.mouseClicked(mouseX, mouseY, button);
        for (NumberFieldBox textField : this.poseTextFields) {
            textField.mouseClicked(mouseX, mouseY, button);
            this.textFieldUpdated();
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    protected void textFieldUpdated() {
        this.updateEntity(this.writeFieldsToNBT());
    }

    private CompoundTag writeFieldsToNBT() {
        CompoundTag compound = new CompoundTag();
        compound.putBoolean("Small", this.smallButton.getValue());
        compound.putBoolean("Locked", this.lockButton.getValue());
        compound.putBoolean("CustomNameVisible", this.nameVisibleButton.getValue());
        compound.putBoolean("NoGravity", this.noGravityButton.getValue());
        compound.putDouble("yOffset", this.YOffsetTextField.getFloat());

        ListTag rotationTag = new ListTag();
        rotationTag.add(FloatTag.valueOf(this.rotationTextField.getFloat()));
        compound.put("Rotation", rotationTag);

        CompoundTag poseTag = new CompoundTag();

        ListTag poseHeadTag = new ListTag();
        poseHeadTag.add(FloatTag.valueOf(this.poseTextFields[0].getFloat()));
        poseHeadTag.add(FloatTag.valueOf(this.poseTextFields[1].getFloat()));
        poseHeadTag.add(FloatTag.valueOf(this.poseTextFields[2].getFloat()));
        poseTag.put("Head", poseHeadTag);

        ListTag poseBodyTag = new ListTag();
        poseBodyTag.add(FloatTag.valueOf(this.poseTextFields[3].getFloat()));
        poseBodyTag.add(FloatTag.valueOf(this.poseTextFields[4].getFloat()));
        poseBodyTag.add(FloatTag.valueOf(this.poseTextFields[5].getFloat()));
        poseTag.put("Body", poseBodyTag);

        ListTag poseLeftLegTag = new ListTag();
        poseLeftLegTag.add(FloatTag.valueOf(this.poseTextFields[6].getFloat()));
        poseLeftLegTag.add(FloatTag.valueOf(this.poseTextFields[7].getFloat()));
        poseLeftLegTag.add(FloatTag.valueOf(this.poseTextFields[8].getFloat()));
        poseTag.put("LeftLeg", poseLeftLegTag);

        ListTag poseRightLegTag = new ListTag();
        poseRightLegTag.add(FloatTag.valueOf(this.poseTextFields[9].getFloat()));
        poseRightLegTag.add(FloatTag.valueOf(this.poseTextFields[10].getFloat()));
        poseRightLegTag.add(FloatTag.valueOf(this.poseTextFields[11].getFloat()));
        poseTag.put("RightLeg", poseRightLegTag);

        ListTag poseLeftArmTag = new ListTag();
        poseLeftArmTag.add(FloatTag.valueOf(this.poseTextFields[12].getFloat()));
        poseLeftArmTag.add(FloatTag.valueOf(this.poseTextFields[13].getFloat()));
        poseLeftArmTag.add(FloatTag.valueOf(this.poseTextFields[14].getFloat()));
        poseTag.put("LeftArm", poseLeftArmTag);

        ListTag poseRightArmTag = new ListTag();
        poseRightArmTag.add(FloatTag.valueOf(this.poseTextFields[15].getFloat()));
        poseRightArmTag.add(FloatTag.valueOf(this.poseTextFields[16].getFloat()));
        poseRightArmTag.add(FloatTag.valueOf(this.poseTextFields[17].getFloat()));
        poseTag.put("RightArm", poseRightArmTag);

        compound.put("Pose", poseTag);
        return compound;
    }
    
    private void updateEntity(CompoundTag compound) {
        CompoundTag CompoundTag = this.playerStatueEntity.saveWithoutId(new CompoundTag()).copy();
        CompoundTag.merge(compound);
        this.playerStatueEntity.load(CompoundTag);

        StatuesNetworking.CHANNEL.send(PacketDistributor.SERVER.noArg(), new PlayerStatueSyncMessage(playerStatueEntity.getUUID(), compound));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
