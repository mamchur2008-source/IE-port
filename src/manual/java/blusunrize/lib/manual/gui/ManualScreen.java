/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual.gui;

import blusunrize.lib.manual.ManualEntry;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.ManualInstance.ManualLink;
import blusunrize.lib.manual.ManualUtils;
import blusunrize.lib.manual.Tree.AbstractNode;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.joml.Matrix3x2fStack;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;

@EventBusSubscriber(value = Dist.CLIENT, modid = "immersiveengineering")
public class ManualScreen extends Screen
{
	private Minecraft mc = Minecraft.getInstance();
	private float scaleFactor = 1;
	private int xSize = 186;
	private int ySize = 198;
	private int guiLeft;
	private int guiTop;
	private List<Button> pageButtons = new ArrayList<>();

	@Nonnull
	public AbstractNode<Identifier, ManualEntry> currentNode;
	public Stack<ManualLink> previousSelectedEntry = new Stack<>();
	public int page;
	public static ManualScreen lastActiveManual;

	ManualInstance manual;
	Identifier texture;
	private double[] lastClick;
	private double[] lastDrag;
	private EditBox searchField;
	private ClickableList entryList;
	private ClickableList suggestionList;

	private final boolean setLastActive;

	public ManualScreen(ManualInstance manual, Identifier texture)
	{
		this(manual, texture, true);
	}

	public ManualScreen(ManualInstance manual, Identifier texture, boolean setLastActive)
	{
		super(Component.literal("manual"));
		this.manual = manual;
		this.currentNode = manual.getRoot();
		this.texture = texture;

		this.setLastActive = setLastActive;
	}

	public ManualEntry getCurrentPage()
	{
		return currentNode.getLeafData();
	}

	public void setCurrentNode(@Nonnull AbstractNode<Identifier, ManualEntry> entry)
	{
		currentNode = entry;
		if(currentNode.isLeaf())
			manual.openEntry(currentNode.getLeafData());
	}

	public ManualInstance getManual()
	{
		return this.manual;
	}

	@Override
	public void init()
	{
		Window res = mc.getWindow();
		double oldGuiScale = res.calculateScale(mc.options.guiScale().get(), mc.isEnforceUnicode());

		int guiScaleInt = Math.min(manual.getGuiRescale(), minecraft.getWindow().calculateScale(0, true));
		double newGuiScale = res.calculateScale(guiScaleInt, true);

		if(guiScaleInt > 0&&newGuiScale!=oldGuiScale)
		{
			scaleFactor = (float)newGuiScale/(float)res.getGuiScale();
			res.setGuiScale((int)newGuiScale);
			width = res.getGuiScaledWidth();
			height = res.getGuiScaledHeight();
			res.setGuiScale((int)oldGuiScale);
		}
		else
			scaleFactor = 1;

		this.manual.openManual();

		guiLeft = (this.width-this.xSize)/2;
		guiTop = (this.height-this.ySize)/2;
		boolean textField = false;

		this.pageButtons.clear();
		if(currentNode.isLeaf())
		{
			currentNode.getLeafData().addButtons(this, guiLeft+32, guiTop+28, page, pageButtons);
			for(Button b : pageButtons)
				addRenderableWidget(b);
		}
		else
		{
			List<AbstractNode<Identifier, ManualEntry>> children = new ArrayList<>();
			for(AbstractNode<Identifier, ManualEntry> node : currentNode.getChildren())
				if(manual.showNodeInList(node))
					children.add(node);
			Consumer<AbstractNode<Identifier, ManualEntry>> openEntry = sel -> {
				if(sel!=null)
				{
					previousSelectedEntry.clear();
					setCurrentNode(sel);
					ManualScreen.this.fullInit();
				}
			};
			entryList = new ClickableList(this, guiLeft+40, guiTop+20, 100, 168,
					1f, children, openEntry);
			addRenderableWidget(entryList);
			suggestionList = new ClickableList(this, guiLeft+180, guiTop+138, 100, 80, 1f,
					new ArrayList<>(), openEntry);
			suggestionList.visible = false;
			addRenderableWidget(suggestionList);
			textField = true;
		}
		if(currentNode.getSuperNode()!=null)
			addRenderableWidget(new GuiButtonManualNavigation(this, guiLeft+24, guiTop+10, 10, 10, 0,
					btn -> {
						if(currentNode.isLeaf()&&!previousSelectedEntry.isEmpty())
							previousSelectedEntry.pop().changePage(ManualScreen.this, false);
						else if(currentNode.getSuperNode()!=null)
							setCurrentNode(currentNode.getSuperNode());
						page = 0;
						ManualScreen.this.fullInit();
					}));

		if(textField)
		{
			searchField = new EditBox(font, guiLeft+166, guiTop+78, 120, 12, Component.empty());
			searchField.setTextColor(-1);
			searchField.setTextColorUneditable(-1);
			searchField.setBordered(false);
			searchField.setMaxLength(17);
			searchField.setFocused(true);
			searchField.setCanLoseFocus(false);
		}
		else if(searchField!=null)
			searchField = null;

		if(setLastActive)
			lastActiveManual = this;
	}

	public void fullInit()
	{
		super.init(width, height);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTime)
	{
		final Matrix3x2fStack transform = graphics.pose();
		transform.pushMatrix();
		if(scaleFactor!=1)
		{
			transform.scale(scaleFactor, scaleFactor);
			mouseX /= scaleFactor;
			mouseY /= scaleFactor;
		}
		super.extractRenderState(graphics, mouseX, mouseY, deltaTime);

		manual.entryRenderPre();

		if(currentNode.isLeaf())
		{
			ManualEntry selectedEntry = currentNode.getLeafData();
			mouseX -= guiLeft;
			mouseY -= guiTop;
			boolean b0 = mouseX > 32&&mouseX < 32+17&&mouseY > 179&&mouseY < 179+10;
			boolean b1 = mouseX > 135&&mouseX < 135+17&&mouseY > 179&&mouseY < 179+10;

			if(page > 0)
				blit(graphics, guiLeft+32, guiTop+179, 0, 216+(b0?20: 0), 16, 10);
			if(page < selectedEntry.getPageCount()-1)
				blit(graphics, guiLeft+136, guiTop+179, 0, 226+(b1?20: 0), 16, 10);

			manual.titleRenderPre();
			//Title
			this.drawCenteredStringScaled(graphics, manual.fontRenderer(), ChatFormatting.BOLD+selectedEntry.getTitle(), guiLeft+xSize/2, guiTop+14, manual.getTitleColour(), true);
			this.drawCenteredStringScaled(graphics, manual.fontRenderer(), manual.formatEntrySubtext(selectedEntry.getSubtext()), guiLeft+xSize/2,
					guiTop+22, manual.getSubTitleColour(), true);
			//Page Number
			this.drawCenteredStringScaled(graphics, manual.fontRenderer(), ChatFormatting.BOLD.toString()+(page+1), guiLeft+xSize/2, guiTop+183, manual.getPagenumberColour(), false);
			manual.titleRenderPost();

			selectedEntry.renderPage(graphics, this, guiLeft+32, guiTop+28, mouseX-32, mouseY-28);

			mouseX += guiLeft;
			mouseY += guiTop;
		}
		else
		{
			String title = ManualUtils.getTitleForNode(currentNode, manual);
			manual.titleRenderPre();
			this.drawCenteredStringScaled(graphics, manual.fontRenderer(), ChatFormatting.BOLD+title, guiLeft+xSize/2, guiTop+12, manual.getTitleColour(), true);
			manual.titleRenderPost();
		}
		if(this.searchField!=null)
		{
			this.searchField.extractRenderState(graphics, mouseX, mouseY, deltaTime);
			if(suggestionList.visible)
				//TODO translation
				graphics.text(manual.fontRenderer(), "It looks like you meant:", guiLeft+180, guiTop+128, manual.getTextColour());
		}
		for(Button btn : pageButtons)
			btn.extractRenderState(graphics, mouseX, mouseY, deltaTime);
		manual.entryRenderPost();
		transform.popMatrix();
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTime)
	{
		// TODO do we want this or not?
		//  super.extractBackground(graphics, mouseX, mouseY, deltaTime);
		blit(graphics, guiLeft, guiTop, 0, 0, xSize, ySize);
		if(this.searchField!=null)
		{
			int l = searchField.getValue().length()*6;
			if(l > 20)
				blit(graphics, guiLeft+166, guiTop+74, 136+(120-l), 238, l, 18);
			if(suggestionList.visible)
			{
				blit(graphics, guiLeft+174, guiTop+100, 214, 212, 16, 26);
				int h = suggestionList.getHeight();
				int w = 76;
				blit(graphics, guiLeft+174, guiTop+116, 230, 212, 16, 16);//Top Left
				blit(graphics, guiLeft+174, guiTop+132+h, 230, 228, 16, 10);//Bottom Left
				blit(graphics, guiLeft+190+w, guiTop+116, 246, 212, 10, 16);//Top Right
				blit(graphics, guiLeft+190+w, guiTop+132+h, 246, 228, 10, 10);//Bottom Right
				for(int hh = 0; hh < h; hh++)
				{
					blit(graphics, guiLeft+174, guiTop+132+hh, 230, 228, 16, 1);
					for(int ww = 0; ww < w; ww++)
						blit(graphics, guiLeft+190+ww, guiTop+132+hh, 246, 228, 1, 1);
					blit(graphics, guiLeft+190+w, guiTop+132+hh, 246, 228, 10, 1);
				}
				for(int ww = 0; ww < w; ww++)
				{
					blit(graphics, guiLeft+190+ww, guiTop+116, 246, 212, 1, 16);
					blit(graphics, guiLeft+190+ww, guiTop+132+h, 246, 228, 1, 10);

				}
			}
		}
	}

	@Override
	public void removed()
	{
		this.manual.closeManual();
		super.removed();
	}

	private void drawCenteredStringScaled(GuiGraphicsExtractor graphics, Font fr, String s, int x, int y, int colour, boolean shadow)
	{
		int xx = (int)Math.floor(x-(fr.width(s)/2.));
		int yy = (int)Math.floor(y-(fr.lineHeight/2.));
		graphics.text(fr, s, xx, yy, colour, shadow);
	}

	private void blit(GuiGraphicsExtractor graphics, int x, int y, int u, int v, int width, int height)
	{
		graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, 256, 256);
	}

	@SubscribeEvent
	public static void appendLinkToTooltip(ItemTooltipEvent ev)
	{
		if(!(Minecraft.getInstance().screen instanceof ManualScreen manualScreen))
			return;
		if(!manualScreen.currentNode.isLeaf())
			return;
		final ItemStack stack = ev.getItemStack();
		if(manualScreen.currentNode.getLeafData().getHighlightedStack(manualScreen.page)==stack)
		{
			ManualLink link = manualScreen.manual.getManualLink(stack);
			if(link!=null)
				ev.getToolTip().add(Component.literal(manualScreen.manual.formatLink(link)));
		}
	}

	@Override
	public boolean mouseScrolled(double x, double y, double wheelX, double wheelY)
	{
		super.mouseScrolled(x, y, wheelX, wheelY);
		if(wheelY!=0&&currentNode.isLeaf())
		{
			if(wheelY > 0&&page > 0)
			{
				page--;
				this.fullInit();
				return true;
			}
			else if(wheelY < 0&&page < currentNode.getLeafData().getPageCount()-1)
			{
				page++;
				this.fullInit();
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick)
	{
		double mx = event.x();
		double my = event.y();
		int button = event.button();
		mx /= scaleFactor;
		my /= scaleFactor;
		if(button==0&&currentNode.isLeaf())
		{
			ManualEntry selectedEntry = currentNode.getLeafData();
			double mxRelative = mx-guiLeft;
			double myRelative = my-guiTop;
			if(page > 0&&mxRelative > 32&&mxRelative < 32+17&&myRelative > 179&&myRelative < 179+10)
			{
				page--;
				this.fullInit();
				return true;
			}
			else if(page < selectedEntry.getPageCount()-1&&mxRelative > 135&&mxRelative < 135+17&&myRelative > 179&&myRelative < 179+10)
			{
				page++;
				this.fullInit();
				return true;
			}
			else
			{
				ItemStack highlighted = selectedEntry.getHighlightedStack(page);
				if(!highlighted.isEmpty())
				{
					ManualLink link = this.getManual().getManualLink(highlighted);
					if(link!=null)
						link.changePage(this, true);
					return true;
				}
			}
		}
		else if(button==1)
		{
			if(searchField!=null&&!searchField.getValue().isEmpty())
				searchField.setValue("");
			else if(currentNode.isLeaf()&&!previousSelectedEntry.isEmpty())
				previousSelectedEntry.pop().changePage(this, false);
			else if(currentNode.getSuperNode()!=null)
			{
				setCurrentNode(currentNode.getSuperNode());
				page = 0;
			}
			this.fullInit();
			return true;
		}
		lastClick = new double[]{mx-guiLeft, my-guiTop};
		MouseButtonEvent scaledEvent = new MouseButtonEvent(mx, my, event.buttonInfo());
		if(super.mouseClicked(scaledEvent, doubleClick))
			return true;
		if(this.searchField!=null)
			this.searchField.mouseClicked(scaledEvent, doubleClick);
		return false;
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event)
	{
		lastClick = null;
		lastDrag = null;
		return super.mouseReleased(event);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY)
	{
		double mx = event.x();
		double my = event.y();
		int button = event.button();
		mx /= scaleFactor;
		my /= scaleFactor;
		if(lastClick!=null&&currentNode.isLeaf())
		{
			if(lastDrag==null)
				lastDrag = new double[]{mx-guiLeft, my-guiTop};
			currentNode.getLeafData().mouseDragged(this, guiLeft+32, guiTop+28, lastClick[0], lastClick[1], mx-guiLeft,
					my-guiTop, lastDrag[0], lastDrag[1], button);
			lastDrag = new double[]{mx-guiLeft, my-guiTop};
			return true;
		}
		return false;
	}

	@Override
	public boolean charTyped(CharacterEvent event)
	{
		if(this.searchField!=null&&this.searchField.charTyped(event))
		{
			updateSearch();
			return true;
		}
		else
			return super.charTyped(event);
	}

	@Override
	public boolean keyPressed(KeyEvent event)
	{
		if(this.searchField!=null&&this.searchField.keyPressed(event))
		{
			updateSearch();
			return true;
		}
		else
			return super.keyPressed(event);
	}

	private void updateSearch()
	{
		String search = searchField.getValue();
		if(search.trim().isEmpty())
		{
			suggestionList.visible = false;
			this.fullInit();
		}
		else
		{
			search = search.toLowerCase(Locale.ENGLISH);
			ArrayList<AbstractNode<Identifier, ManualEntry>> lHeaders = new ArrayList<>();
			Set<AbstractNode<Identifier, ManualEntry>> lSpellcheck = new HashSet<>();
			final String searchFinal = search;
			manual.getAllEntriesAndCategories().forEach((node) ->
			{
				if(manual.showNodeInList(node))
				{
					String title = ManualUtils.getTitleForNode(node, manual).toLowerCase(Locale.ENGLISH);
					if(title.contains(searchFinal))
						lHeaders.add(node);
					else
						lSpellcheck.add(node);
				}
			});
			List<AbstractNode<Identifier, ManualEntry>> lCorrections =
					ManualUtils.getPrimitiveSpellingCorrections(search, lSpellcheck, 4,
							(e) -> ManualUtils.getTitleForNode(e, manual));
			for(AbstractNode<Identifier, ManualEntry> node : lSpellcheck)
				if(!lCorrections.contains(node))
				{
					if(node.isLeaf()&&node.getLeafData().listForSearch(search))
					{
						lHeaders.add(node);
						lCorrections.add(node);
						break;
					}
				}

			entryList.setEntries(lHeaders);
			if(!lCorrections.isEmpty())
				suggestionList.setEntries(lCorrections);
			suggestionList.visible = !lCorrections.isEmpty();
		}
	}

	@Override
	public boolean isPauseScreen()
	{
		return false;
	}
}
