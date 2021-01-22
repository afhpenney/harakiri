package me.vaxry.harakiri.impl.patch;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.api.event.EventStageable;
import me.vaxry.harakiri.api.event.minecraft.EventDisplayGui;
import me.vaxry.harakiri.api.event.minecraft.EventKeyPress;
import me.vaxry.harakiri.api.event.minecraft.EventRunTick;
import me.vaxry.harakiri.api.event.minecraft.EventUpdateFramebufferSize;
import me.vaxry.harakiri.api.event.mouse.EventMouseLeftClick;
import me.vaxry.harakiri.api.event.mouse.EventMouseRightClick;
import me.vaxry.harakiri.api.event.world.EventLoadWorld;
import me.vaxry.harakiri.api.patch.ClassPatch;
import me.vaxry.harakiri.api.patch.MethodPatch;
import me.vaxry.harakiri.api.util.ASMUtil;
import me.vaxry.harakiri.impl.management.PatchManager;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import org.lwjgl.input.Keyboard;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * Author Seth
 * 4/4/2019 @ 11:27 PM.
 */
public final class MinecraftPatch extends ClassPatch {

    public MinecraftPatch() {
        super("net.minecraft.client.Minecraft", "bib");
    }

    /**
     * Patch the method "updateFramebufferSize"
     * Mainly used for shaders
     *
     * @param methodNode
     */
    @MethodPatch(
            mcpName = "updateFramebufferSize",
            notchName = "aC",
            mcpDesc = "()V")
    public void updateFramebufferSize(MethodNode methodNode, PatchManager.Environment env) {
        //inset a static method call to our method "updateFramebufferSizeHook"
        methodNode.instructions.insert(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "updateFramebufferSizeHook", "()V", false));
    }

    /**
     * This is called when we resize our game
     */
    public static void updateFramebufferSizeHook() {
        //dispatch our event "EventUpdateFramebufferSize"
        Harakiri.INSTANCE.getEventManager().dispatchEvent(new EventUpdateFramebufferSize());
    }

    /**
     * Patch the method "runTick"
     * The bytecode we are inserting here replicates this call
     * MinecraftPatch.runTickHook(PRE);
     *
     * @param methodNode
     * @param env
     */
    @MethodPatch(
            mcpName = "runTick",
            notchName = "t",
            mcpDesc = "()V")
    public void runTick(MethodNode methodNode, PatchManager.Environment env) {
        //create a list of instructions and add the needed instructions to call our hook function
        final InsnList preInsn = new InsnList();
        //PRE
        preInsn.add(new FieldInsnNode(GETSTATIC, "me/vaxry/harakiri/api/event/EventStageable$EventStage", "PRE", "Lme/vaxry/harakiri/api/event/EventStageable$EventStage;"));
        //MinecraftPatch.runTickHook();
        preInsn.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "runTickHook", "(Lme/vaxry/harakiri/api/event/EventStageable$EventStage;)V", false));
        //insert the list of instructions at the top of the function
        methodNode.instructions.insert(preInsn);

        //do the same thing as above but insert the list at the bottom of the method
        final InsnList postInsn = new InsnList();
        //POST
        postInsn.add(new FieldInsnNode(GETSTATIC, "me/vaxry/harakiri/api/event/EventStageable$EventStage", "POST", "Lme/vaxry/harakiri/api/event/EventStageable$EventStage;"));
        //MinecraftPatch.runTickHook();
        postInsn.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "runTickHook", "(Lme/vaxry/harakiri/api/event/EventStageable$EventStage;)V", false));
        //insert the list of instructions at the bottom of the function
        methodNode.instructions.insertBefore(ASMUtil.bottom(methodNode), postInsn);
    }

    /**
     * This is twice called every tick
     */
    public static void runTickHook(EventStageable.EventStage stage) {
        //dispatch our event "EventRunTick" and pass in the stage(pre, post)
        Harakiri.INSTANCE.getEventManager().dispatchEvent(new EventRunTick(stage));
    }

    /**
     * This is where key input is handled
     *
     * @param methodNode
     * @param env
     */
    @MethodPatch(
            mcpName = "runTickKeyboard",
            notchName = "aD",
            mcpDesc = "()V")
    public void runTickKeyboard(MethodNode methodNode, PatchManager.Environment env) {
        //find the instruction that calls dispatchKeypresses
        final AbstractInsnNode target = ASMUtil.findMethodInsn(methodNode, INVOKEVIRTUAL, env == PatchManager.Environment.IDE ? "net/minecraft/client/Minecraft" : "bib", env == PatchManager.Environment.IDE ? "dispatchKeypresses" : "W", "()V");

        if (target != null) {
            //make a list of instructions
            final InsnList insnList = new InsnList();
            //we use ILOAD to pass the "keycode" into our call
            insnList.add(new VarInsnNode(ILOAD, 1));
            //call our hook function
            insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "runTickKeyboardHook", "(I)V", false));
            //inset the instructions after the call "dispatchKeypresses"
            methodNode.instructions.insert(target, insnList);
        }
    }

    /**
     * This is a hacky way to intercept key presses
     */
    public static void runTickKeyboardHook(int key) {
        //check if the key was just pressed
        if (Keyboard.getEventKeyState()) {
            //dispatch our event for key presses and pass in the keycode
            Harakiri.INSTANCE.getEventManager().dispatchEvent(new EventKeyPress(key));
        }
    }

    /**
     * This is used to tell if we just opened a gui screen
     * It can be cancelled
     *
     * @param methodNode
     * @param env
     */
    @MethodPatch(
            mcpName = "displayGuiScreen",
            notchName = "a",
            mcpDesc = "(Lnet/minecraft/client/gui/GuiScreen;)V",
            notchDesc = "(Lblk;)V")
    public void displayGuiScreen(MethodNode methodNode, PatchManager.Environment env) {
        //make a list of our instructions
        final InsnList insnList = new InsnList();
        //we use ALOAD to pass the first param to our call
        insnList.add(new VarInsnNode(ALOAD, 1));
        //call our hook function
        //note the desc is ()Z because our hook function is a boolean
        insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "displayGuiScreenHook", env == PatchManager.Environment.IDE ? "(Lnet/minecraft/client/gui/GuiScreen;)Z" : "(Lblk;)Z", false));
        //create a LabelNode to jump to
        final LabelNode jmp = new LabelNode();
        //add an "if equals" and pass our label
        insnList.add(new JumpInsnNode(IFEQ, jmp));
        //add a return
        insnList.add(new InsnNode(RETURN));
        //finally add the label
        insnList.add(jmp);
        //insert our instructions at the top
        methodNode.instructions.insert(insnList);
    }

    /**
     * Our display gui hook called when we open a gui
     *
     * @param screen can be null!
     * @return
     */
    public static boolean displayGuiScreenHook(GuiScreen screen) {
        //dispatch our event and pass the gui
        final EventDisplayGui event = new EventDisplayGui(screen);
        Harakiri.INSTANCE.getEventManager().dispatchEvent(event);

        //return event.isCanceled() to allow us to cancel the original function
        return event.isCanceled();
    }

    @MethodPatch(
            mcpName = "loadWorld",
            notchName = "a",
            mcpDesc = "(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V",
            notchDesc = "(Lbsb;Ljava/lang/String;)V")
    public void loadWorld(MethodNode methodNode, PatchManager.Environment env) {
        final InsnList insnList = new InsnList();
        insnList.add(new VarInsnNode(ALOAD, 1));
        insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "loadWorldHook", env == PatchManager.Environment.IDE ? "(Lnet/minecraft/client/multiplayer/WorldClient;)Z" : "(Lbsb;)Z", false));
        final LabelNode jmp = new LabelNode();
        insnList.add(new JumpInsnNode(IFEQ, jmp));
        insnList.add(new InsnNode(RETURN));
        insnList.add(jmp);
        methodNode.instructions.insert(insnList);
    }

    public static boolean loadWorldHook(WorldClient worldClient) {
        final EventLoadWorld event = new EventLoadWorld(worldClient);
        Harakiri.INSTANCE.getEventManager().dispatchEvent(event);
        return event.isCanceled();
    }

    @MethodPatch(
            mcpName = "clickMouse",
            notchName = "aA",
            mcpDesc = "()V",
            notchDesc = "()V")
    public void clickMouse(MethodNode methodNode, PatchManager.Environment env) {
        final InsnList insnList = new InsnList();
        insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "clickMouseHook", "()Z", false));
        final LabelNode jmp = new LabelNode();
        insnList.add(new JumpInsnNode(IFEQ, jmp));
        insnList.add(new InsnNode(RETURN));
        insnList.add(jmp);
        methodNode.instructions.insert(insnList);
    }

    public static boolean clickMouseHook() {
        final EventMouseLeftClick event = new EventMouseLeftClick();
        Harakiri.INSTANCE.getEventManager().dispatchEvent(event);
        return event.isCanceled();
    }

    @MethodPatch(
            mcpName = "rightClickMouse",
            notchName = "aB",
            mcpDesc = "()V",
            notchDesc = "()V")
    public void rightClickMouse(MethodNode methodNode, PatchManager.Environment env) {
        final InsnList insnList = new InsnList();
        insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "rightClickMouseHook", "()Z", false));
        final LabelNode jmp = new LabelNode();
        insnList.add(new JumpInsnNode(IFEQ, jmp));
        insnList.add(new InsnNode(RETURN));
        insnList.add(jmp);
        methodNode.instructions.insert(insnList);
    }

    public static boolean rightClickMouseHook() {
        final EventMouseRightClick event = new EventMouseRightClick();
        Harakiri.INSTANCE.getEventManager().dispatchEvent(event);
        return event.isCanceled();
    }
}
